package pl.edu.mimuw.cloudatlas.fetcher;

import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SystemInformationCollector {
    public static final int DNS_MAX = 3;
    private Collection<Attribute> attributes;
    private SystemInfo systemInfo;

    public SystemInformationCollector(Collection<Attribute> attributes) {
        this.attributes = attributes;
        systemInfo = new SystemInfo();
    }

    public AttributesMap collect() {
        AttributesMap map = new AttributesMap();
        for (Attribute attribute: attributes) {
            map.add(attribute, getInfo(attribute));
        }
        return map;
    }

    private Value getInfo(Attribute attribute) {
        switch (attribute.getName()) {
            case "cpu_load":
                return new ValueDouble(systemInfo.getHardware().getProcessor().getSystemCpuLoadBetweenTicks());
            case "free_disk":
                Long free_space = 0L;
                for (OSFileStore disc : systemInfo.getOperatingSystem().getFileSystem().getFileStores())
                    free_space += disc.getUsableSpace();
                return new ValueInt(free_space);
            case "total_disk":
                Long total_space = 0L;
                for (OSFileStore disc : systemInfo.getOperatingSystem().getFileSystem().getFileStores())
                    total_space += disc.getTotalSpace();
                return new ValueInt(total_space);
            case "free_ram":
                return new ValueInt(systemInfo.getHardware().getMemory().getAvailable());
            case "total_ram":
                return new ValueInt(systemInfo.getHardware().getMemory().getTotal());
            case "free_swap":
                return new ValueInt(systemInfo.getHardware().getMemory().getSwapTotal() - systemInfo.getHardware().getMemory().getSwapUsed());
            case "total_swap":
                return new ValueInt(systemInfo.getHardware().getMemory().getSwapTotal());
            case "num_processes":
                return new ValueInt((long) systemInfo.getOperatingSystem().getProcessCount());
            case "num_cores":
                return new ValueInt((long) systemInfo.getHardware().getProcessor().getPhysicalProcessorCount());
            case "kernel_ver":
                return new ValueString(systemInfo.getOperatingSystem().getVersion().getBuildNumber());
            case "logged_users":
                Value user_num = null;
                try {
                    Process process = Runtime.getRuntime().exec("who -q");
                    process.waitFor();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String[] users = bufferedReader.readLine().split(" ");
                    int user_num_int = (new HashSet<>(Arrays.asList(users))).size();
                    user_num = new ValueInt((long) user_num_int);
                } catch (IOException | InterruptedException e) {
                    user_num = ValueNull.getInstance();
                }
                return user_num;
            case "dns_names":
                String[] dns_array = systemInfo.getOperatingSystem().getNetworkParams().getDnsServers();
                ValueList dns_list = new ValueList(TypePrimitive.STRING);
                for (int i = 0; i < Math.min(DNS_MAX, dns_array.length); i++)
                    dns_list.add(new ValueString(dns_array[i]));
                return dns_list;
            default:
                return ValueNull.getInstance();
        }
    }
}
