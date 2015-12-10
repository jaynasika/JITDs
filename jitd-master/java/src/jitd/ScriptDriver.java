package jitd;


import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import org.apache.logging.log4j.Logger;

import org.astraldb.util.GetArgs;

import jitd.SplayBST;
import jitd.mfa.CountEntry;
import jitd.mfa.FrequencyException;
import jitd.mfa.LossyCounting;

public class ScriptDriver {

	public static final long READ_WIDTH = 1000;// 2*1000*1000;

	private static Logger log = 
			org.apache.logging.log4j.LogManager.getLogger();
	public Driver driver = new Driver(new Mode(), null);
	KeyValueIterator rand;
	long timer = 0, start = 0;
	int op = 0;
	boolean fixedMode = false;
	
	// Added variables for splay functionality and 
	int splayInterval = -1;
	String splayMethod = "";
	long totalSplayTime = 0;
	
	// graphString is used as identifier for naming the graph
	public StringBuffer graphString = new StringBuffer();
	

	public ArrayCog array(int size)
	{
		ArrayCog cog = new ArrayCog(size);
		cog.load(rand);
		return cog;
	}

	public void startTime() { start = System.nanoTime(); }
	public long endTime(LogType type) 
	{ 
		long end = System.nanoTime();
		long delta = (end-start)/1000;
		timer += delta;
		log.info("{} ({}): {} us", type, op, delta);
		timeLog.add(new LogEntry(type, delta));
		return delta;
	}

	public long splayEndTime(LogType type) 
	{ 
		long end = System.nanoTime();
		long delta = (end-start)/1000;
		totalSplayTime += delta;
		log.info("{} ({}): {} us", type, op, delta);
		timeLog.add(new LogEntry(type, delta));
		return delta;
	}

	public void init(int baseSize, String distributionMode)
	{
		log.info("Load: {} records", baseSize);
		if ((distributionMode != null) &&
				(distributionMode.equalsIgnoreCase("zipf"))) {
			rand = new KeyValueIterator.ZipfianIterator(baseSize);
			graphString.append("zipf_");
		} else {
			rand = new KeyValueIterator.RandomIterator();
			graphString.append("random_");
		}		
		graphString.append(baseSize + "_");
		driver.root = array(baseSize);
	}

	public void write(int writeSize)
	{
		op++;
		Cog cog = array(writeSize);
		startTime();
		driver.insert(cog);
		endTime(LogType.WRITE);
		graphString.append("write" + writeSize + "_");
	}

	public long randKey()
	{ return rand.randKey(); }

	public void read()
	{ read(randKey()); }

	public void read(long start)
	{ read(start, start+READ_WIDTH); }


	public void read(long start, long end)
	{
		op++;
		log.info("Read for: {}--{}", start, end); 
		startTime();
		KeyValueIterator iter = driver.scan(start, end);
		endTime(LogType.READ);
		log.trace("Record Count Is: {}", driver.root.length());
	}

	public void readWithMFA() {
		readWithMFA(randKey());
	}

	public void readWithMFA(long start) {
		readWithMFA(start, start + READ_WIDTH);
	}

	public void readWithMFA(long start, long end)
	{		
		op++;
		log.info("Read for: {}--{}", start, end); 
		startTime();
		KeyValueIterator iter = driver.scan(start, end);
		try {
			((CrackerMode)driver.mode).frequentItemsCounter.add(start);
		} catch (FrequencyException e) {
			log.error("Error while adding item to frequentItemsCounter", e);
			e.printStackTrace();
		}
		endTime(LogType.READ);
		log.trace("Record Count Is: {}", driver.root.length());
	}

	public void seqRead(int count)
	{ seqRead(count, READ_WIDTH); }
	
	public void splay(long key) {
		op++;
		log.info("Splay around: {}", key); 
		startTime();
		driver.root = SplayBST.splayTheCog(driver.root,key);
		splayEndTime(LogType.SPLAY);
		log.trace("New Root Is: {}", ((BTreeCog)driver.root).sep);
	}
	
	// This method is used only for demo purpose to show the splay process!
	public void splayNow(String splayMethod) {
		long key;
		if(splayMethod.equals("lowerBound")) {
			key =randKey();    	
			read(key, key + READ_WIDTH);
			splay(key);
		} else if(splayMethod.equals("median")) {
			key = SplayBST.findMedianKey(driver.root, ((CrackerMode)driver.mode).seperatorCount);
			splay(key);
		}
	}

	public void seqRead(int count, long width)
	{
		switch(splayMethod) {
		case "lowerBound":
			seqReadSplayMedianOrLowerBound(count, width, false);
			break;
		case "median": 
			seqReadSplayMedianOrLowerBound(count, width, true);
			break;
		case "MFA":
			//int counters = 20;
			//double probabilityOfFailure = 0.2;
			double support = 0.03;
			double maxError = 0.003;
			((CrackerMode)driver.mode).frequentItemsCounter = new LossyCounting<Long>(maxError);
			seqReadSplayWithMFA(count, width, support);
			break;
		default:
			splayMethod = "NoSplay";			
			for(int i = 0; i < count; i++) {
				long randKey =randKey();
				read(randKey, randKey + width);
			}
		}		
		graphString.append("seqRead_");
		graphString.append(splayMethod + "_");
		graphString.append(count);
		System.out.println(graphString);
	}

	private void seqReadSplayWithMFA(int count, long width, double support) {
		for(int i = 0; i < count; i++) {
			long key = randKey();
			readWithMFA(key, key + width);
			if((i+1) % splayInterval == 0){	
				List<CountEntry<Long>> topk = ((CrackerMode)driver.mode).frequentItemsCounter.peek(20);
				System.out.println(topk.size());
				// System.out.println(topk);
				// print the items
				for (int indx = topk.size() - 1; indx >= 0 ; indx--) {
					CountEntry<Long> item = topk.get(indx);
					// System.out.print(item.item + " ");
					key = item.item;
					splay(key);
				}
			}    
		}
	}

	private void seqReadSplayMedianOrLowerBound(int count, long width, boolean isMedian) {
		for(int i = 0; i < count; i++) {
			long key =randKey();    	
			read(key, key + width);
			if((i+1) % splayInterval == 0){
				if (isMedian) {
					key = SplayBST.findMedianKey(driver.root, ((CrackerMode)driver.mode).seperatorCount);
				}
				splay(key);
			}    
		}
	}

	public Mode modeForString(String mode){
		switch(mode.toLowerCase()){
		case "naive"       : return new Mode();
		case "cracker"     : return new CrackerMode();
		case "merge"       : return new PushdownMergeMode();
		case "simplemerge" : return new MergeMode();
		case "enhancedmerge" : return new EnhancedMergeMode();
		default: 
			log.fatal("Unknown Mode '{}'", mode);
			System.exit(-1);
			return null;
		}
	}

	public void setMode(String mode)
	{
		if(fixedMode){ return; }
		graphString.append(mode + "_");
		driver.mode = modeForString(mode);
	}

	public void transition(String source, String target, int steps)
	{
		if(fixedMode){ return; }
		driver.mode = new TransitionMode(modeForString(source), modeForString(target), steps);
	}

	public  void dump()
	{
		System.out.println(driver.root.toString());
	}

	public void seedPRNG(int seed)
	{
		rand.setSeed(seed);
	}

	public void setSplayInterval(int splayInterval) {
		this.splayInterval = splayInterval;
		graphString.append("splayAt_" + splayInterval + "_");
	}

	public void exec(String cmd)
	{
		cmd = cmd.replaceAll("[ \n\r]*(#.*)?$", "").replaceAll("^ *", "");
		log.trace("Exec: {}", cmd);
		if(cmd.equals("")){ return; }
		String[] args = cmd.split(" +");

		switch(args[0]){
		case "init":
			if (args.length > 2) {
				init(Integer.parseInt(args[1]),args[2]);
			} else {
				System.out.println("null mode distrib");
				init(Integer.parseInt(args[1]),null);
			}
			break;

		case "write":
			write(Integer.parseInt(args[1]));
			break;

		case "read":
			//read();
			if(args.length==1){
				read();
			}
			else if (args.length==2){
				read(Integer.parseInt(args[1]));
			}
			else{
				read(Integer.parseInt(args[1]),Integer.parseInt(args[2]));
			}
			break;

		case "seqread":
			seqRead(Integer.parseInt(args[1]));
			break;

			//      case "seqreadwrite":
			//        seqReadWrite(Integer.parseInt(args[1]), Integer.parseInt(args[2]), 
			//                     Float.parseFloat(args[3]));
			//        break;

		case "mode":
			setMode(args[1]);
			break;

		case "transition":
			transition(args[1], args[2], Integer.parseInt(args[3]));
			break;

		case "dump":
			dump();
			break;

		case "seed":
			seedPRNG(Integer.parseInt(args[1]));
			break;

		case "splayInterval":
			setSplayInterval(Integer.parseInt(args[1]));
			break;
		case "splayMethod":
			setSplayMethod(args[1]);
			break;

		default:
			log.fatal("Unknown command '{}' ('{}')", args[0], cmd);
			System.exit(-1);
		}

	}

	public void setSplayMethod(String splayMethod) {
		this.splayMethod = splayMethod;
	}

	public void execStream(Reader r)
			throws IOException
	{
		BufferedReader br = new BufferedReader(r);
		String line;
		while((line = br.readLine()) != null){
			exec(line);
		}
	}

	public static void main(String argString[])
			throws IOException
	{
		GetArgs args = new GetArgs(argString);
		String arg;
		ScriptDriver sd = new ScriptDriver();
		while((arg = args.nextArg()) != null){
			log.trace("Arg: {}", arg);
			switch(arg){
			case "--mode":
				sd.setMode(args.nextArg());
				sd.fixedMode = true;
				break;
			default: 
				log.fatal("Unknown argument '{}'", arg);
			}
		}

		for(String file : args.getFiles()){
			if(file.equals("-")){
				sd.execStream(new InputStreamReader(System.in));
			} else {
				sd.execStream(new FileReader(file));
			}
		}
		//avgAndWriteToFile(sd.timeLog, 10, sd.graphString.toString() + "_" + sd.timer);
		log.info("Total Time: {}", sd.timer);
		//System.out.println("Total splay time in microsecs : " + sd.totalSplayTime/1000);

		//sd.dump();
	}
	public static void avgAndWriteToFile(List<LogEntry> timelog, int avgFindingInterval, String filename) {
		long subsetSum = 0;
		double avg = 0.0;
		Map<Integer,Double> timeIntervalToAvgTimeTaken = new TreeMap<>();
		for(int i=0;i<timelog.size();i++) {
			//System.out.println(logentry.type + ":" +logentry.time);
			if ((i+1)%avgFindingInterval == 0) {
				avg = subsetSum / avgFindingInterval;
				subsetSum = 0;
				timeIntervalToAvgTimeTaken.put(i+1, avg);
			} else {
				subsetSum = subsetSum + timelog.get(i).time;
			}
		}	

		try {			
			File file = new File("/home/bade/workspace/JITDs/jitd-master/java/bin/" 
					+ filename + ".out");

			// if file doesnt exists, then create it 
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Entry<Integer,Double> entry : timeIntervalToAvgTimeTaken.entrySet()) {
				bw.write(entry.getKey() + ":" + entry.getValue());
				bw.newLine();
			}
			bw.close();
			System.out.println(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void resetLog()
	{
		timeLog.clear();
	}

	public final List<LogEntry> timeLog = new ArrayList<LogEntry>();

	public enum LogType { READ, WRITE, SPLAY }

	public static class LogEntry {
		public final LogType type;
		public final long time;
		public LogEntry(LogType type, long time)
		{ this.type = type; this.time = time; }
	}


}