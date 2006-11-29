package org.apache.ode.store.hib;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.store.Messages;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.msg.MessageBundle;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;

public class DbConfStoreConnectionFactory implements ConfStoreConnectionFactory {
    private static final Log __log = LogFactory.getLog(DbConfStoreConnectionFactory.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";

    private static final HashMap<String, DialectFactory.VersionInsensitiveMapper> HIBERNATE_DIALECTS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();

    private static final String _guid = new GUID().toString();

    private static final Map<String, DataSource> _dataSources = new ConcurrentHashMap<String, DataSource>();

    static {
        // Hibernate has a nice table that resolves the dialect from the database
        // product name,
        // but doesn't include all the drivers. So this is supplementary, and some
        // day in the
        // future they'll add more drivers and we can get rid of this.
        // Drivers already recognized by Hibernate:
        // HSQL Database Engine
        // DB2/NT
        // MySQL
        // PostgreSQL
        // Microsoft SQL Server Database, Microsoft SQL Server
        // Sybase SQL Server
        // Informix Dynamic Server
        // Oracle 8 and Oracle >8
        HIBERNATE_DIALECTS.put("Apache Derby", new DialectFactory.VersionInsensitiveMapper("org.hibernate.dialect.DerbyDialect"));
    }

    private final DataSource _ds;

    final SessionFactory _sessionFactory;

    public DbConfStoreConnectionFactory(DataSource ds, boolean auto) {
        _ds = ds;

        Properties properties = new Properties();

        __log.debug("using data source: " + ds);
        _dataSources.put(_guid, ds);
        properties.put("guid", _guid);
        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        
        try {
            properties.put(Environment.DIALECT, guessDialect(_ds));
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
            __log.error(errmsg, ex);
            throw new BpelEngineException(errmsg, ex);
        }
        
        if (auto) {
            properties.put(Environment.HBM2DDL_AUTO, "create-drop");
        }

        _sessionFactory = getDefaultConfiguration().setProperties(properties).buildSessionFactory();

    }

    public ConfStoreConnectionHib getConnection() {
        return new ConfStoreConnectionHib(_sessionFactory.openSession());
    }

    private String guessDialect(DataSource dataSource) throws Exception {

        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = dataSource.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                __log.info("Using database " + dbProductName + " major version " + dbMajorVer);
                DialectFactory.DatabaseDialectMapper mapper = HIBERNATE_DIALECTS.get(dbProductName);
                if (mapper != null) {
                    dialect = mapper.getDialectClass(dbMajorVer);
                } else {
                    Dialect hbDialect = DialectFactory.determineDialect(dbProductName, dbMajorVer);
                    if (hbDialect != null)
                        dialect = hbDialect.getClass().getName();
                }
            }
        } finally {
            conn.close();
        }

        if (dialect == null) {
            __log.info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        return dialect;

    }

    static Configuration getDefaultConfiguration() throws MappingException {
        return new Configuration().addClass(ProcessConfDaoImpl.class).addClass(DeploymentUnitDaoImpl.class);
    }

    public static class DataSourceConnectionProvider implements ConnectionProvider {
        private String _guid;

        public DataSourceConnectionProvider() {
        }

        public void configure(Properties props) throws HibernateException {
            _guid = props.getProperty("guid");
        }

        public Connection getConnection() throws SQLException {
            return _dataSources.get(_guid).getConnection();
        }

        public void closeConnection(Connection arg0) throws SQLException {
            arg0.close();
        }

        public void close() throws HibernateException {
        }

        public boolean supportsAggressiveRelease() {
            return true;
        }
    }

}
