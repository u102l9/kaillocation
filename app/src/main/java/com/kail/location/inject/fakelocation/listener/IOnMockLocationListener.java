package com.kail.location.inject.fakelocation.listener;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;

public interface IOnMockLocationListener extends IInterface {

    abstract class Stub extends Binder implements IOnMockLocationListener {

        private static class Proxy implements IOnMockLocationListener {
            public static IOnMockLocationListener defaultImpl;

            private IBinder remoteBinder;

            Proxy(IBinder remoteBinder) {
                this.remoteBinder = remoteBinder;
            }

            @Override
            public IBinder asBinder() {
                return this.remoteBinder;
            }

            @Override
            public void onMockLocationChanged(String provider, int status, boolean enabled) {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken("com.kail.location.ipc.IOnMockLocationListener");
                    data.writeString(provider);
                    data.writeInt(status);
                    data.writeInt(enabled ? 1 : 0);
                    if (this.remoteBinder.transact(1, data, null, 1) || Stub.getDefaultImpl() == null) {
                        return;
                    }
                    Stub.getDefaultImpl().onMockLocationChanged(provider, status, enabled);
                } catch (android.os.RemoteException ignored) {
                } finally {
                    data.recycle();
                }
            }
        }

        public static IOnMockLocationListener asInterface(IBinder binder) {
            if (binder == null) {
                return null;
            }
            IInterface localInterface = binder.queryLocalInterface("com.kail.location.ipc.IOnMockLocationListener");
            return (localInterface == null || !(localInterface instanceof IOnMockLocationListener)) ? new Proxy(binder) : (IOnMockLocationListener) localInterface;
        }

        public static IOnMockLocationListener getDefaultImpl() {
            return Proxy.defaultImpl;
        }
    }

    void onMockLocationChanged(String provider, int status, boolean enabled);
}
