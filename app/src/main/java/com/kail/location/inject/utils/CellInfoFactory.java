package com.kail.location.inject.utils;

import android.location.Location;
import android.os.Build;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.ClosedSubscriberGroupInfo;
import android.telephony.NeighboringCellInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;

public class CellInfoFactory {
    private static Location currentLocation;

    public static CellIdentity getCellIdentity(CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoGsm) {
            return ((CellInfoGsm) cellInfo).getCellIdentity();
        }
        if (cellInfo instanceof CellInfoCdma) {
            return ((CellInfoCdma) cellInfo).getCellIdentity();
        }
        if (cellInfo instanceof CellInfoLte) {
            return ((CellInfoLte) cellInfo).getCellIdentity();
        }
        if (cellInfo instanceof CellInfoWcdma) {
            return ((CellInfoWcdma) cellInfo).getCellIdentity();
        }
        return null;
    }

    public static void setCurrentLocation(Location location) {
        currentLocation = location;
    }

    public static CellInfo createCellInfo(CellTowerInfo cellTowerInfo) {
        if (cellTowerInfo == null) {
            return null;
        }
        String radioType = cellTowerInfo.getRadioType();
        radioType.hashCode();
        switch (radioType) {
            case "GSM":
                return createGsmCellInfo(cellTowerInfo);
            case "LTE":
                return createLteCellInfo(cellTowerInfo);
            case "CDMA":
                return createCdmaCellInfo(cellTowerInfo);
            case "UMTS":
            case "WCDM":
                return createWcdmaCellInfo(cellTowerInfo);
            default:
                return createCdmaCellInfo(cellTowerInfo);
        }
    }

    private static CellInfoCdma createCdmaCellInfo(CellTowerInfo cellTowerInfo) {
        Object cellIdentity;
        try {
            int longitude = (int) (cellTowerInfo.getLongitude() * 14400.0d);
            int latitude = (int) (cellTowerInfo.getLatitude() * 14400.0d);
            if (Build.VERSION.SDK_INT >= 29) {
                Class intType = Integer.TYPE;
                cellIdentity = ReflectionUtils.newInstance(CellIdentityCdma.class, new Class[]{intType, intType, intType, intType, intType, String.class, String.class}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf(cellTowerInfo.getMnc()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(longitude), Integer.valueOf(latitude), null, null});
            } else {
                Class intType = Integer.TYPE;
                cellIdentity = ReflectionUtils.newInstance(CellIdentityCdma.class, new Class[]{intType, intType, intType, intType, intType}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf(cellTowerInfo.getMnc()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(longitude), Integer.valueOf(latitude)});
            }
            CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) cellIdentity;
            Class intType = Integer.TYPE;
            CellSignalStrengthCdma signalStrength = (CellSignalStrengthCdma) ReflectionUtils.newInstance(CellSignalStrengthCdma.class, new Class[]{intType, intType, intType, intType, intType}, new Object[]{-64, -60, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE});
            ReflectionUtils.setFieldValue(signalStrength, CellSignalStrengthCdma.class, "mLevel", 4);
            CellInfoCdma cellInfoCdma = (CellInfoCdma) ReflectionUtils.newInstance(CellInfoCdma.class, null, null);
            ReflectionUtils.setFieldValue(cellInfoCdma, CellInfoCdma.class, "mCellIdentityCdma", cellIdentityCdma);
            ReflectionUtils.setFieldValue(cellInfoCdma, CellInfoCdma.class, "mCellSignalStrengthCdma", signalStrength);
            ReflectionUtils.setFieldValue(cellInfoCdma, CellInfoCdma.class, "mRegistered", Boolean.TRUE);
            ReflectionUtils.setFieldValue(cellInfoCdma, CellInfoCdma.class, "mTimeStamp", 171027930794631L);
            return cellInfoCdma;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private static CellInfoGsm createGsmCellInfo(CellTowerInfo cellTowerInfo) {
        CellIdentityGsm cellIdentityGsm;
        CellInfoGsm cellInfoGsm;
        Class<CellInfoGsm> cellInfoClass;
        try {
            int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt >= 30) {
                Class intType = Integer.TYPE;
                cellIdentityGsm = (CellIdentityGsm) ReflectionUtils.newInstance(CellIdentityGsm.class, new Class[]{intType, intType, intType, intType, String.class, String.class, String.class, String.class, Collection.class}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.MAX_VALUE, Integer.MAX_VALUE, String.format("%03d", Integer.valueOf(cellTowerInfo.getMcc())), String.format("%02d", Integer.valueOf(cellTowerInfo.getMnc())), null, null, Collections.emptyList()});
                cellInfoGsm = (CellInfoGsm) ReflectionUtils.newInstance(CellInfoGsm.class, null, null);
                cellInfoClass = CellInfoGsm.class;
            } else if (sdkInt >= 29) {
                Class intType = Integer.TYPE;
                cellIdentityGsm = (CellIdentityGsm) ReflectionUtils.newInstance(CellIdentityGsm.class, new Class[]{intType, intType, intType, intType, String.class, String.class, String.class, String.class}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.MAX_VALUE, Integer.MAX_VALUE, String.format("%03d", Integer.valueOf(cellTowerInfo.getMcc())), String.format("%02d", Integer.valueOf(cellTowerInfo.getMnc())), null, null});
                cellInfoGsm = (CellInfoGsm) ReflectionUtils.newInstance(CellInfoGsm.class, null, null);
                cellInfoClass = CellInfoGsm.class;
            } else {
                Class intType = Integer.TYPE;
                cellIdentityGsm = (CellIdentityGsm) ReflectionUtils.newInstance(CellIdentityGsm.class, new Class[]{intType, intType, intType, intType}, new Object[]{Integer.valueOf(cellTowerInfo.getMcc()), Integer.valueOf(cellTowerInfo.getMnc()), Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId())});
                cellInfoGsm = (CellInfoGsm) ReflectionUtils.newInstance(CellInfoGsm.class, null, null);
                cellInfoClass = CellInfoGsm.class;
            }
            ReflectionUtils.setFieldValue(cellInfoGsm, cellInfoClass, "mCellIdentityGsm", cellIdentityGsm);
            return cellInfoGsm;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private static CellInfoLte createLteCellInfo(CellTowerInfo cellTowerInfo) {
        try {
            Class intType = Integer.TYPE;
            CellIdentityLte cellIdentityLte = (CellIdentityLte) ReflectionUtils.newInstance(CellIdentityLte.class, new Class[]{intType, intType, intType, intType, intType}, new Object[]{Integer.valueOf(cellTowerInfo.getMcc()), Integer.valueOf(cellTowerInfo.getMnc()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(cellTowerInfo.getPsc()), Integer.valueOf(cellTowerInfo.getLac())});
            CellInfoLte cellInfoLte = (CellInfoLte) ReflectionUtils.newInstance(CellInfoLte.class, null, null);
            ReflectionUtils.setFieldValue(cellInfoLte, CellInfoLte.class, "mCellIdentityLte", cellIdentityLte);
            return cellInfoLte;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private static CellInfoWcdma createWcdmaCellInfo(CellTowerInfo cellTowerInfo) {
        Object cellIdentity;
        try {
            int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt >= 30) {
                Class intType = Integer.TYPE;
                cellIdentity = ReflectionUtils.newInstance(CellIdentityWcdma.class, new Class[]{intType, intType, intType, intType, String.class, String.class, String.class, String.class, Collection.class, ClosedSubscriberGroupInfo.class}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(cellTowerInfo.getPsc()), Integer.MAX_VALUE, String.format("%03d", Integer.valueOf(cellTowerInfo.getMcc())), String.format("%02d", Integer.valueOf(cellTowerInfo.getMnc())), null, null, Collections.emptyList(), null});
            } else if (sdkInt >= 29) {
                Class intType = Integer.TYPE;
                cellIdentity = ReflectionUtils.newInstance(CellIdentityWcdma.class, new Class[]{intType, intType, intType, intType, String.class, String.class, String.class, String.class}, new Object[]{Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(cellTowerInfo.getPsc()), Integer.MAX_VALUE, String.format("%03d", Integer.valueOf(cellTowerInfo.getMcc())), String.format("%02d", Integer.valueOf(cellTowerInfo.getMnc())), null, null});
            } else {
                Class intType = Integer.TYPE;
                cellIdentity = ReflectionUtils.newInstance(CellIdentityWcdma.class, new Class[]{intType, intType, intType, intType, intType}, new Object[]{Integer.valueOf(cellTowerInfo.getMcc()), Integer.valueOf(cellTowerInfo.getMnc()), Integer.valueOf(cellTowerInfo.getLac()), Integer.valueOf((int) cellTowerInfo.getCellId()), Integer.valueOf(cellTowerInfo.getPsc())});
            }
            CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) cellIdentity;
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) ReflectionUtils.newInstance(CellInfoWcdma.class, null, null);
            ReflectionUtils.setFieldValue(cellInfoWcdma, CellInfoWcdma.class, "mCellIdentityWcdma", cellIdentityWcdma);
            return cellInfoWcdma;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static List<CellInfo> createCellInfoList(List<CellTowerInfo> cellTowerInfos) {
        if (cellTowerInfos == null || cellTowerInfos.isEmpty()) {
            return null;
        }
        ArrayList cellInfos = new ArrayList();
        Iterator<CellTowerInfo> iterator = cellTowerInfos.iterator();
        while (iterator.hasNext()) {
            CellInfo cellInfo = createCellInfo(iterator.next());
            if (cellInfo != null) {
                cellInfos.add(cellInfo);
            }
        }
        return cellInfos;
    }

    public static NeighboringCellInfo createNeighboringCellInfo(CellTowerInfo cellTowerInfo) {
        if (cellTowerInfo == null) {
            return null;
        }
        try {
            NeighboringCellInfo neighboringCellInfo = (NeighboringCellInfo) ReflectionUtils.newInstance(NeighboringCellInfo.class, null, null);
            ReflectionUtils.setFieldValue(neighboringCellInfo, NeighboringCellInfo.class, "mRssi", -46);
            ReflectionUtils.setFieldValue(neighboringCellInfo, NeighboringCellInfo.class, "mCid", Integer.valueOf((int) cellTowerInfo.getCellId()));
            ReflectionUtils.setFieldValue(neighboringCellInfo, NeighboringCellInfo.class, "mLac", Integer.valueOf(cellTowerInfo.getLac()));
            ReflectionUtils.setFieldValue(neighboringCellInfo, NeighboringCellInfo.class, "mPsc", Integer.valueOf(cellTowerInfo.getPsc()));
            ReflectionUtils.setFieldValue(neighboringCellInfo, NeighboringCellInfo.class, "mNetworkType", 3);
            return neighboringCellInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<NeighboringCellInfo> createNeighboringCellInfoList(List<CellTowerInfo> cellTowerInfos) {
        if (cellTowerInfos == null || cellTowerInfos.isEmpty()) {
            return null;
        }
        ArrayList neighboringCellInfos = new ArrayList();
        Iterator<CellTowerInfo> iterator = cellTowerInfos.iterator();
        while (iterator.hasNext()) {
            NeighboringCellInfo neighboringCellInfo = createNeighboringCellInfo(iterator.next());
            if (neighboringCellInfo != null) {
                neighboringCellInfos.add(neighboringCellInfo);
            }
        }
        return neighboringCellInfos;
    }
}
