package com.github.enokiy.deserialization.utils;

//import sun.misc.ObjectInputFilter;
import sun.rmi.registry.RegistryImpl;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public class MyRegistryImpl extends RegistryImpl {
    public MyRegistryImpl(int i, RMIClientSocketFactory rmiClientSocketFactory, RMIServerSocketFactory rmiServerSocketFactory) throws RemoteException {
        super(i, rmiClientSocketFactory, rmiServerSocketFactory);
    }

//    public MyRegistryImpl(int i, RMIClientSocketFactory rmiClientSocketFactory, RMIServerSocketFactory rmiServerSocketFactory, ObjectInputFilter objectInputFilter) throws RemoteException {
//        super(i, rmiClientSocketFactory, rmiServerSocketFactory, objectInputFilter);
//    }

    public MyRegistryImpl(int i) throws RemoteException {
        super(i);
    }

    @Override
    public Remote lookup(String s) throws RemoteException, NotBoundException {
        System.out.println("RMI: "+ s);
        return super.lookup(s);
    }
}
