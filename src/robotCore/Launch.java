/*
 * Copyright (c) 2013 Creative Sphere Limited.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 * Contributors:
 *
 *   Creative Sphere - initial API and implementation
 *
 */
package robotCore;

import org.ah.java.remotevmlauncher.client.LaunchRemote;

/**
 * Main class to launch remote application on a machine with running agent.
 *
 * @author Daniel Sendula
 */
public class Launch {
    public static void main(String[] args) throws Exception {
        LaunchRemote client = new LaunchRemote();
        
//        System.out.println(String.format("Launch: %d", args.length));
        
        client.run(args);

//        int debugLevel = 0;
//
//        boolean remoteClasspath = false;
//        boolean excludeClasspath = false;
//        boolean addressAndPort = true;
//        boolean mainClass = true;
//        boolean arguments = false;
//        boolean allRead = false;
//        boolean debugLevelFlag = false;
//        boolean remoteDebugPortFlag = false;
//        boolean remoteVMarg = false;
//        for (String arg : args) {
//            if (!arguments) {
//                if (remoteClasspath) {
//                    client.remoteClasspathProcessor.getRemoteClasspath().add(arg);
//                    remoteClasspath = false;
//                } else if (excludeClasspath) {
//                    client.excludeClassPath.add(arg);
//                    excludeClasspath = false;
//                } else if (debugLevelFlag) {
//                    debugLevel = Integer.parseInt(arg);
//                    client.startRemoteVMProcessor.getLauncherArgs().add("-d");
//                    client.startRemoteVMProcessor.getLauncherArgs().add(Integer.toString(Integer.parseInt(arg)));
//                    debugLevelFlag = false;
//                } else if (remoteDebugPortFlag) {
//                    client.startRemoteVMProcessor.setRemoteDebugPort(Integer.parseInt(arg));
//                    remoteDebugPortFlag = false;
//                } else if (remoteVMarg) {
//                    client.startRemoteVMProcessor.getVmArgs().add(arg);
//                    remoteVMarg = false;
//                } else if ("-rcp".equals(arg) || "--remote-classpath".equals(arg)) {
//                    remoteClasspath = true;
//                } else if ("-ecp".equals(arg) || "--exclude-classpath".equals(arg)) {
//                    excludeClasspath = true;
//                } else if ("-d".equals(arg) || "--debug".equals(arg)) {
//                    debugLevelFlag = true;
//                } else if ("-rdp".equals(arg) || "--remote-debug-port".equals(arg)) {
//                    remoteDebugPortFlag = true;
//                } else if ("-rds".equals(arg) || "--remote-debug-suspend".equals(arg)) {
//                    client.startRemoteVMProcessor.setRemoteDebugSuspendAtStart(true);
//                } else if ("-rvma".equals(arg) || "--remote-VM-argment".equals(arg)) {
//                    remoteVMarg = true;
//                } else if ("-h".equals(arg) || "--help".equals(arg)) {
//                    printHelp();
//                    System.exit(0);
//                } else if (addressAndPort) {
//                    int i = arg.indexOf(':');
//                    if (i < 0) {
//                        client.setRemoteAgentSocketAddress(new InetSocketAddress(Integer.parseInt(arg)));
//                    } else {
//                        client.setRemoteAgentSocketAddress(new InetSocketAddress(arg.substring(0, i), Integer.parseInt(arg.substring(i + 1))));
//                    }
//                    addressAndPort = false;
//                    mainClass = true;
//                } else if (mainClass) {
//                    client.mainClassProcessor.setMainClass(arg);
//                    mainClass = false;
//                    allRead = true;
//                } else if (allRead) {
//                    if ("--".equals(arg)) {
//                        allRead = false;
//                        arguments = true;
//                    } else {
//                        System.out.println("ERROR: unknown argument '" + arg + "'");
//                        System.out.println();
//                        printHelp();
//                        System.exit(1);
//                    }
//                } else {
//                    System.out.println("ERROR: unknown argument '" + arg + "'");
//                    System.out.println();
//                    printHelp();
//                    System.exit(1);
//                }
//            } else {
//                client.argumentsProcessor.getArguments().add(arg);
//            }
//        }
//        if (addressAndPort) {
//            System.out.println("ERROR: missing address and port argument.");
//            System.out.println();
//            printHelp();
//            System.exit(1);
//        }
//        if (mainClass) {
//            System.out.println("ERROR: missing main class argument.");
//            System.out.println();
//            printHelp();
//            System.exit(1);
//        }
//
//        JavaLoggingUtils.setupSimpleConsoleLogging(debugLevel);
//
//        client.setup();
//
//        client.start();
    }

    public static void printHelp() {
        System.out.println("Remote VM Launcher usage:");
        System.out.println("");
        System.out.println("java -jar remotevmlauncher-client.jar {options} [address:]port mainClass");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("");
        System.out.println("  -rcp|--remote-classpath classpath-entry");
        System.out.println("                      classpath entry as seen from remote machine");
        System.out.println("                      where agent is running.");
        System.out.println("  -ecp|--exclude-classpath classpath-entry");
        System.out.println("                      classpath entry not to be send to the remote");
        System.out.println("                      machine where agent is running.");
        System.out.println("  -d|--debug level    debug level from 0 to 4. Default: 0");
        System.out.println("  -rdp|--remote-debug-port port ");
        System.out.println("                      If port is specified remote VM will be launched");
        System.out.println("                      in debug mode at the specified port.");
        System.out.println("  -rds|--remote-debug-suspend ");
        System.out.println("                      If port is specified and if this flag as well");
        System.out.println("                      remote VM will be suspended.");
        System.out.println("  -h|--help           this help.");
        System.out.println("");
        System.out.println("If launcher is used from an IDE, and ");
        System.out.println("   org.ah.java.remotevmlauncher.client.LaunchRemote class is directly used as");
        System.out.println("main class, then supplied classpath will automatically used (provided that it");
        System.out.println("is defined as URLClassLoader or descendant class) on the remote side.");
        System.out.println("");
        System.out.println("Note: if remote debug port is specified launcher's VM is started with:");
        System.out.println("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=(y|n),address=<port>");
        System.out.println("depending on port and if suspend is specified or not.");
   }

}
