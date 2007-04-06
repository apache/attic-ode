module Buildr
  module Java
    module Hibernate

      REQUIRES = OpenObject.new
      REQUIRES.collections  = "commons-collections:commons-collections:jar:3.1"
      REQUIRES.logging      = "commons-logging:commons-logging:jar:1.0.3"
      REQUIRES.dom4j        = "dom4j:dom4j:jar:1.6.1"
      REQUIRES.hibernate    = "org.hibernate:hibernate:jar:3.1.2"
      REQUIRES.xdoclet      = group("xdoclet", "xdoclet-xdoclet-module", "xdoclet-hibernate-module",
                                    :under=>"xdoclet", :version=>"1.2.3") + ["xdoclet:xjavadoc:jar:1.1-j5"]

      class << self
        include Ant

        # Uses XDoclet to generate HBM files form annotated source files.
        # Options include:
        # * :sources -- Directory (or directories) containing source files.
        # * :target -- The target directory.
        # * :excludetags -- Tags to exclude (see HibernateDocletTask)
        #
        # For example:
        #  Java::Hibernate.xdoclet :sources=>compile.sources,
        #    :target=>compile.target, :excludedtags=>"@version,@author,@todo"
        def xdoclet(options)
          ant("hibernatedoclet") do |ant|
            ant.taskdef :name=>"hibernatedoclet", :classname=>"xdoclet.modules.hibernate.HibernateDocletTask", :classpath=>requires
            ant.hibernatedoclet :destdir=>options[:target].to_s, :excludedtags=>options[:excludedtags], :force=>"true" do
              hibernate :version=>"3.0"
              options[:sources].to_a.each do |source|
                fileset :dir=>source.to_s, :includes=>"**/*.java"
              end
            end
          end
        end

        # Returns a new AntProject that supports the schemaexport task.
        def schemaexport(name = "schemaexport")
          ant(name) do |ant|
            ant.taskdef :name=>"schemaexport", :classname=>"org.hibernate.tool.hbm2ddl.SchemaExportTask", :classpath=>requires
          end
        end

        # Returns an new task with an accessor (ant) to an AntProject that supports
        # the schemaexport task.
        #
        # For example:
        #   Java::Hibernate.schemaexport_task.enhance do |task|
        #     task.ant.schemaexport :properties=>"derby.properties", :output=>"derby.sql",
        #       :delimiter=>";", :drop=>"no", :create=>"yes" do
        #       fileset(:dir=>path_to(:java_src_dir)) { include :name=>"**/*.hbm.xml" } }
        #     end
        #   end
        def schemaexport_task(name = "schemaexport")
          unless Rake::Task.task_defined?(name)
            class << task(name) ; attr_accessor :ant ; end
            task(name).enhance { |task| task.ant = schemaexport(name) }
          end
          task(name)
        end

      protected

        # This will download all the required artifacts before returning a classpath, and we want to do this only once.
        def requires()
          @requires ||= artifacts(REQUIRES.to_hash.values).each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
        end

      end

    end
  end
end
