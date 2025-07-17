package com.example.agent.module;

import com.example.agent.Impl.MonitorModule;
import org.json.JSONArray;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import org.json.JSONObject;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

public class OsMonitorModule implements MonitorModule {
    private SystemInfo si = new SystemInfo();



    @Override
    public String getName() {
        return "os";
    }



    @Override
    public JSONObject collectData() {
        JSONObject osData = new JSONObject();

        // CPU 정보
        // CPU 정보
        CentralProcessor cpu = si.getHardware().getProcessor();
        OperatingSystem os = si.getOperatingSystem();

        // CPU 최대 클럭(GHz)
        double maxFreqGHz = cpu.getMaxFreq() / 1_000_000_000.0;

        // CPU 부하율 계산 (이전 틱 저장, 1초 대기 후 계산)
        long[] prevTicks = cpu.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // 예외 처리
        }
        long[] ticks = cpu.getSystemCpuLoadTicks();
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // CPU 여유율
        double cpuIdle = 100.0 - cpuLoad;

        // 프로세스 및 스레드 수
        int processCount = ((OperatingSystem) os).getProcessCount();
        int threadCount = ((OperatingSystem) os).getThreadCount();

        JSONObject cpuJson = new JSONObject();
        cpuJson.put("cpuLoadPercent", String.format("%.2f", cpuLoad));
        cpuJson.put("cpuIdlePercent", String.format("%.2f", cpuIdle));
        cpuJson.put("processCount", processCount);
        cpuJson.put("threadCount", threadCount);

        // 핸들러 수는 OS별로 다르므로 별도 구현 필요 (예: 리눅스는 /proc/sys/fs/file-nr)
        // 필요시 확장 가능

        osData.put("cpu", cpuJson);

        // 메모리 정보
        GlobalMemory memory = si.getHardware().getMemory();
        long totalMem = memory.getTotal();
        long availableMem = memory.getAvailable();
        long usedMem = totalMem - availableMem;
        double memUsagePercent = totalMem == 0 ? 0 : (usedMem * 100.0 / totalMem);

        JSONObject memJson = new JSONObject();
        memJson.put("totalMemoryGB", String.format("%.2f", bytesToGB(totalMem)));
        memJson.put("availableMemoryGB", String.format("%.2f", bytesToGB(availableMem)));
        memJson.put("usedMemoryGB", String.format("%.2f", bytesToGB(usedMem)));
        memJson.put("memoryUsagePercent", String.format("%.2f", memUsagePercent));

        osData.put("memory", memJson);

        // 디스크 파티션 정보
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
        OSFileStore[] fsArray = fileSystem.getFileStores().toArray(new OSFileStore[0]);

        JSONArray diskArray = new JSONArray();

        for (OSFileStore fs : fsArray) {
            long totalSpace = fs.getTotalSpace();
            long usableSpace = fs.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;
            double usagePercent = totalSpace == 0 ? 0 : (usedSpace * 100.0 / totalSpace);

            JSONObject fsJson = new JSONObject();
            String status = getUsageStatus(usagePercent);

            fsJson.put("name", fs.getName());
            fsJson.put("mountPoint", fs.getMount());
            fsJson.put("type", fs.getType());
            fsJson.put("totalSpaceGB", String.format("%.2f", bytesToGB(totalSpace)));
            fsJson.put("usableSpaceGB", String.format("%.2f", bytesToGB(usableSpace)));
            fsJson.put("usedSpaceGB", String.format("%.2f", bytesToGB(usedSpace)));
            fsJson.put("usagePercent", String.format("%.2f", usagePercent));
            fsJson.put("status", status);

            diskArray.put(fsJson);
        }

        osData.put("partitions", diskArray);

        return osData;
    }


    private static double bytesToGB(long bytes) {
        return bytes / 1073741824.0;
    }

    private static double hertzToGHz(long hz) {
        return hz / 1000000000.0;
    }

    private String getUsageStatus(double usagePercent) {
        if (usagePercent < 70) {
            return "여유";
        } else if (usagePercent < 95) {
            return "주의";
        } else {
            return "위험";
        }
    }
}