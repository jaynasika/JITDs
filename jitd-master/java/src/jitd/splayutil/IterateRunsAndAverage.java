package jitd.splayutil;
import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jitd.ScriptDriver;

public class IterateRunsAndAverage {

	public static void main(String args[]) throws InterruptedException {
		final int numOfIterations = 1;
		Map<String, String> InputFileToavgExecTime = new TreeMap<>();
		for (String inputTestFile : args) {
			try {
				String[] cmd = new String[3];
				cmd[0] = "sh";
				cmd[1] = "/home/bade/workspace/JITDs/jitd-master/java/bin/jitdsim";
				cmd[2] = "/home/bade/workspace/JITDs/tests/" + inputTestFile + ".sim";
				System.out.println("--------Input File : " + inputTestFile + "--------");
				long overallExecTime = 0;
				for (int i = 1; i<=numOfIterations; i++) {
					String outputFile = "/home/bade/workspace/JITDs/jitd-master/java/bin/results/" 
							+ inputTestFile + "_" + i;

					ProcessBuilder builder = new ProcessBuilder(cmd);
					builder.redirectOutput(new File(outputFile + ".out"));
					builder.redirectError(new File(outputFile + ".err"));
					Process p = builder.start();
					p.waitFor();
					/*BufferedReader stdInput = new BufferedReader(new
							InputStreamReader(p.getInputStream()));
					String line,last = null;
					while ((line = stdInput.readLine()) != null) {
						last = line;
					}
					System.out.println("Iter : " + i + "\t" + last);
					String[] splitByColon = last.split(" +");
					Long execTime = Long.parseLong(splitByColon[splitByColon.length-1].trim());
					overallExecTime = overallExecTime + execTime;*/
				}
				/*Long avgExecutionTime = (overallExecTime/numOfIterations);
				System.out.println("Avg Execution Time : " + avgExecutionTime);
				InputFileToavgExecTime.put(avgExecutionTime.toString(), inputTestFile);*/
				System.out.println("--------End Input File : " + inputTestFile + "--------");
			}
			catch (IOException e) {
				System.out.println("exception happened - here's what I know: ");
				e.printStackTrace();
				System.exit(-1);
			}			
		}
		for (Entry<String,String> entry : InputFileToavgExecTime.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());				
		}
	}
}