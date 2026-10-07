package com.kail.location.inject.utils;

import android.location.Location;
import android.telephony.SubscriptionInfo;
import java.util.List;
import com.kail.location.inject.fakelocation.model.CellTowerInfo;

/**
 * 注入进程读取模拟状态的统一入口。
 *
 * <p>原实现走 {@code oem_location} binder，但本机 SELinux 既拒绝 system_server
 * 动态 {@code addService}（{@code add_service oem_location=false}），也拒绝目标
 * 进程（如 {@code com.android.phone} 的 radio 域）{@code find} 该服务。因此彻底
 * 弃用 binder，改为 <b>Provider 优先（{@link ConfigProviderChannel}）、文件兜底</b>。
 * Provider 读写日志见 {@link ConfigProviderChannel}。
 */
public final class MockLocationServiceManager {

    private static final class Holder {
        static MockLocationServiceManager instance = new MockLocationServiceManager();
    }

    private MockLocationServiceManager() {
    }

    public static MockLocationServiceManager getInstance() {
        return Holder.instance;
    }

    public List<String> getAllowMockPackages() {
        try {
            AllowMockPackagesConfigFile.Config cfg = AllowMockPackagesConfigFile.read();
            return cfg.enabled && !cfg.packages.isEmpty() ? cfg.packages : null;
        } catch (Throwable t) {
            return null;
        }
    }

    public List<CellTowerInfo> getMockCells() {
        // Provider 优先。
        String cell = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_CELL_CONFIG);
        if (cell != null) {
            CellMockConfigFile.Config cfg = CellMockConfigFile.parse(cell);
            return cfg.enabled && !cfg.towers.isEmpty() ? cfg.towers : null;
        }
        // 文件兜底。
        try {
            CellMockConfigFile.Config cfg = CellMockConfigFile.read();
            return cfg.enabled && !cfg.towers.isEmpty() ? cfg.towers : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /** 位置模拟由 system_server 直接派发，进程内镜像不再需要，返回 null。 */
    public Location getMockLocation() {
        return null;
    }

    public List<SubscriptionInfo> getMockSubscriptionInfo() {
        return null;
    }

    public List<String> getSafeApps() {
        return null;
    }

    public boolean isMockGpsStatusEnabled() {
        return false;
    }

    public boolean isMocking() {
        // Provider 优先。
        String cell = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_CELL_CONFIG);
        if (cell != null) {
            CellMockConfigFile.Config cfg = CellMockConfigFile.parse(cell);
            if (cfg.enabled && !cfg.towers.isEmpty()) {
                return true;
            }
        }
        String wifi = ConfigProviderChannel.get(LocationShm.PROVIDER_KEY_WIFI_CONFIG);
        if (wifi != null) {
            WifiMockConfigFile.Config cfg = WifiMockConfigFile.parse(wifi);
            if (cfg.enabled && !cfg.networks.isEmpty()) {
                return true;
            }
        }
        // 文件兜底。
        try {
            CellMockConfigFile.Config cellCfg = CellMockConfigFile.read();
            if (cellCfg.enabled && !cellCfg.towers.isEmpty()) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        try {
            WifiMockConfigFile.Config wifiCfg = WifiMockConfigFile.read();
            if (wifiCfg.enabled && !wifiCfg.networks.isEmpty()) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
