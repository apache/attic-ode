package org.apache.ode.bpel.runtime.channels;

import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.ap.ChannelType;

@ChannelType
public interface ReadWriteLock {

    public void readLock(SynchChannel s);
    
    public void writeLock(SynchChannel s);
    
    public void unlock(SynchChannel s);
}
