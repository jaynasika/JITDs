package jitd.splayutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jitd.ArrayCog;
import jitd.CrackerMode;
import jitd.Driver;
import jitd.KeyValueIterator;
import jitd.Mode;
import jitd.SplayBST;
import jitd.mfa.CountEntry;
import jitd.mfa.FrequencyException;
import jitd.mfa.LossyCounting;

public class CompareSplayAffect {
	public static void main(String[] args) {
		long readWidth = 1000;
		int dataSize = 1000000;
		KeyValueIterator.ZipfianIterator iter = new KeyValueIterator.ZipfianIterator(dataSize);
		ArrayCog initcog = new ArrayCog(dataSize);
		initcog.load(iter);
		int numberOfReads = 100;
		List<Long> lowsList = new ArrayList<>(numberOfReads);		
		for(int i = 0; i < numberOfReads; i++) {
			lowsList.add(iter.randKey());
		}
		/*experiment(initcog, lowsList, readWidth, -1, "nosplay");
		experiment(initcog, lowsList, readWidth, -1, "nosplay");
		experiment(initcog, lowsList, readWidth, 100, "lowerBound");
		experiment(initcog, lowsList, readWidth, 100, "median");
*/		experiment(initcog, lowsList, readWidth, 100, "frequentItems");
	}

	public static void reloadCogValues(ArrayCog src, ArrayCog dest) {
		for (int i=0; i<src.length; i++) {
			dest.keys[i] = src.keys[i];
			dest.values[i] = src.values[i];
		}
	}

	public static void experiment(ArrayCog initcog, List<Long> lowsList, long readWidth, int splayInterval, String splayMethod) {
		ArrayCog arraycog = new ArrayCog(initcog.length);	  
		reloadCogValues(initcog, arraycog);
		Mode mode = new CrackerMode();
		Driver driver = new Driver(mode, arraycog);
		long timer = -1;
		switch(splayMethod){
		case "lowerBound":
			timer = seqReadSplayMedianOrLowerBound(driver,lowsList,readWidth,splayInterval,false);
			break;
		case "median": 
			timer = seqReadSplayMedianOrLowerBound(driver,lowsList,readWidth,splayInterval,true);
			break;
		case "frequentItems":
			//int counters = 20;
			//double probabilityOfFailure = 0.2;
			double support = 0.003;
			double maxError = 0.0003;
			((CrackerMode)driver.mode).frequentItemsCounter = new LossyCounting<Long>(maxError);
			timer = seqReadSplayWithMFA(driver, lowsList, readWidth, splayInterval, support);
			break;
		default:
			splayMethod = "No Splay";			
			timer = seqReadNoSplay(driver,lowsList,readWidth);
			break;
		}		
		System.out.println("Splay at : " + splayInterval  + "\t" 
				+ " using method : " + splayMethod  +  " takes " + timer + " microsecs");
	}

	public static long seqReadSplayMedianOrLowerBound(Driver driver, List<Long> lowsList, long readWidth, int splayInterval, boolean isMedian)
	{
		long timer = 0;
		long splaytimer = 0;
		//System.out.println(lowsList.toString());
		for(int i = 0; i < lowsList.size(); i++) {
			Long low = lowsList.get(i);
			long startreadtime = System.nanoTime();
			driver.scan(low, low + readWidth);
			long endreadtime = System.nanoTime();
			long delta = (endreadtime-startreadtime)/1000;
			timer += delta;
			if((i+1) % splayInterval == 0){
				long key = low;
				if (isMedian) {
					key = SplayBST.findMedianKey(driver.root, ((CrackerMode)driver.mode).seperatorCount);
				}
				long startsplaytime = System.nanoTime();
				driver.root = SplayBST.splayTheCog(driver.root,key);
				long endsplaytime = System.nanoTime();
				long splayTime = ((endsplaytime - startsplaytime));
				splaytimer += splayTime;
				//System.out.print( splayTime + "::" );
			}    
		}
		//System.out.println("\nSplay Time : " + splaytimer);

		return timer;
	}


	public static long seqReadNoSplay(Driver driver, List<Long> lowsList, long readWidth) {
		long timer = 0;
		//System.out.println(lowsList.toString());
		for(int i = 0; i < lowsList.size(); i++) {
			Long low = lowsList.get(i);
			long startreadtime = System.nanoTime();
			driver.scan(low, low + readWidth);
			long endreadtime = System.nanoTime();
			long delta = (endreadtime-startreadtime)/1000;
			timer += delta;
		}
		return timer;
	}

	public static long seqReadSplayWithMFA(Driver driver, List<Long> lowsList, long readWidth, int splayInterval, double support)
	{
		long timer = 0;
		long splaytimer = 0;
		//System.out.println(lowsList.toString());
		for(int i = 0; i < lowsList.size(); i++) {
			Long low = lowsList.get(i);
			long startreadtime = System.nanoTime();
			driver.scan(low, low + readWidth);
			try {
				((CrackerMode)driver.mode).frequentItemsCounter.add(low);
			} catch (FrequencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long endreadtime = System.nanoTime();
			long delta = (endreadtime-startreadtime)/1000;
			timer += delta;
			if((i+1) % splayInterval == 0){					
				long startsplaytime = System.nanoTime();
				List<CountEntry<Long>> topk = ((CrackerMode)driver.mode).frequentItemsCounter.peek(20);
				System.out.println(topk);
				long key = low;
				// print the items
				for (int indx = topk.size() - 1; indx >=0 ; indx--) {
					CountEntry<Long> item = topk.get(indx);
					//System.out.println(item);
					key = item.item;
					driver.root = SplayBST.splayTheCog(driver.root,key);
				}					
				SplayBST.print("",true,driver.root);
				long endsplaytime = System.nanoTime();
				long splayTime = ((endsplaytime - startsplaytime))/1000;
				splaytimer += splayTime;
				//System.out.print( splayTime + "::" );
			}    
		}
		//System.out.println("\nSplay Time : " + splaytimer);
		return timer + splaytimer;
	}

	@SuppressWarnings("rawtypes")
	public static Set printMostFrequentElements(List<Long> lowsList) {
		Map<Long, Integer> valueToCountMap = new HashMap<>();
		for (Long elem : lowsList) {
			Integer count = valueToCountMap.get(elem);
			if(count == null) {
				valueToCountMap.put(elem, 1);
			} else {
				valueToCountMap.put(elem, count+1);
			}
		}

		// Convert Map to List
		List<Map.Entry<Long, Integer>> list = 
				new LinkedList<Map.Entry<Long, Integer>>(valueToCountMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Long, Integer>>() {
			public int compare(Map.Entry<Long, Integer> o1,
					Map.Entry<Long, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<Long, Integer> sortedMap = new LinkedHashMap<Long, Integer>();
		for (Iterator<Map.Entry<Long, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Long, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		};
		int i=1;
		Set<Long> set = new HashSet<>();
		int totalcount = 0;
		for (Entry<Long, Integer> e : sortedMap.entrySet()) {
			if (i<=30) {
				//System.out.println(e.getKey() + ":" + e.getValue());
				set.add(e.getKey());
				totalcount +=e.getValue();
				i++;
			} else break;
		}
		//System.out.println(totalcount);
		return set;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void printStatsOfMFASet(Set targetSet, Set actualSet) {
		Set set3 = new HashSet<>(targetSet);
		System.out.println(targetSet.size() + " " + actualSet.size() + " " + set3.size());
		targetSet.removeAll(actualSet);
		System.out.println("Items missing in actualSet(False negatives) : " + targetSet.size());
		System.out.println(targetSet.size() + " " + actualSet.size() + " " + set3.size());
		actualSet.removeAll(set3);
		System.out.println("Items extra in actualSet(False positives) : " + actualSet.size());
		System.out.println(targetSet.size() + " " + actualSet.size() + " " + set3.size());
	}
}