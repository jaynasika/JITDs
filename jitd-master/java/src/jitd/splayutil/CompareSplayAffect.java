package jitd.splayutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jitd.ArrayCog;
import jitd.CrackerMode;
import jitd.Driver;
import jitd.KeyValueIterator;
import jitd.Mode;
import jitd.SplayBST;

public class CompareSplayAffect {
	public static void main(String[] args) {
		long readWidth = 10;

		int dataSize = 1000000;
		KeyValueIterator.ZipfianIterator iter = new KeyValueIterator.ZipfianIterator(dataSize);
		iter.KEY_RANGE = 1000;
		ArrayCog initcog = new ArrayCog(dataSize);
		initcog.load(iter);
		int numberOfReads = 1000;
		List<Long> lowsList = new ArrayList<>(numberOfReads);		
		for(int i = 0; i < numberOfReads; i++) {
			lowsList.add(iter.randKey());
		}
		// printMostFrequentElements(lowsList);
		experiment(initcog, lowsList, readWidth, -1);
		experiment(initcog, lowsList, readWidth, -1);
		experiment(initcog, lowsList, readWidth, 100);
	}

	public static void reloadCogValues(ArrayCog src, ArrayCog dest) {
		for (int i=0; i<src.length; i++) {
			dest.keys[i] = src.keys[i];
			dest.values[i] = src.values[i];
		}
	}

	public static void experiment(ArrayCog initcog, List<Long> lowsList, long readWidth, int splayInterval) {
		ArrayCog arraycog = new ArrayCog(initcog.length);	  
		reloadCogValues(initcog, arraycog);
		Mode mode = new CrackerMode();
		Driver driver = new Driver(mode, arraycog);
		System.out.println("Splay at : " + splayInterval  + "\t"  +  seqRead(driver,lowsList,readWidth,splayInterval));
	}

	public static long seqRead(Driver driver, List<Long> lowsList, long readWidth, int splayInterval)
	{
		long timer = 0;
		long splaytimer = 0;
		//System.out.println(lowsList.toString());
		if (splayInterval > -1) {
			for(int i = 0; i < lowsList.size(); i++) {
				Long low = lowsList.get(i);
				long startreadtime = System.currentTimeMillis();
				driver.scan(low, low + readWidth);
				long endreadtime = System.currentTimeMillis();
				long delta = (endreadtime-startreadtime);
				timer += delta;
				if((i+1) % splayInterval == 0){
					long key = SplayBST.findMedianKey(driver.root, ((CrackerMode)driver.mode).sepratorCount);
					long startsplaytime = System.nanoTime();
					driver.root = SplayBST.splayTheCog(driver.root,key);
					long endsplaytime = System.nanoTime();
					long splayTime = ((endsplaytime - startsplaytime));
					splaytimer += splayTime;
					System.out.print( splayTime + "::" );
				}    
			}
			System.out.println("\nSplay Time : " + splaytimer);
		} else {
			for(int i = 0; i < lowsList.size(); i++) {
				Long low = lowsList.get(i);
				long startreadtime = System.currentTimeMillis();
				driver.scan(low, low + readWidth);
				long endreadtime = System.currentTimeMillis();
				long delta = (endreadtime-startreadtime);
				timer += delta;
			}
		}
		return timer;
	}

	public static void printMostFrequentElements(List<Long> lowsList) {
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
		int i=0;
		for (Entry<Long, Integer> e : sortedMap.entrySet()) {
			if (i<=20) {
				System.out.println(e.getKey() + ":" + e.getValue());
				i++;
			} else break;
		}
	}
}