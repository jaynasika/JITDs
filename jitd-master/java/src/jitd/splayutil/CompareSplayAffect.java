package jitd.splayutil;

import java.util.ArrayList;
import java.util.List;

import jitd.ArrayCog;
import jitd.CrackerMode;
import jitd.Driver;
import jitd.KeyValueIterator;
import jitd.Mode;
import jitd.SplayBST;

public class CompareSplayAffect {
	public static void main(String[] args) {
		int dataSize = 1000000;
		long readWidth = 10;
		int numberOfReads = 1000;

		KeyValueIterator.ZipfianIterator iter = new KeyValueIterator.ZipfianIterator(dataSize);
		iter.KEY_RANGE = 1000;

		ArrayCog initcog = new ArrayCog(dataSize);
		initcog.load(iter);

		CrackerMode mode = new CrackerMode();	  

		List<Long> lowsList = new ArrayList<>();
		for(int i = 0; i < numberOfReads; i++) {
			lowsList.add(iter.randKey());
		}

	  experiment(initcog, mode, lowsList, readWidth, -1);
	  experiment(initcog, mode, lowsList, readWidth, -1);
	  experiment(initcog, mode, lowsList, readWidth, -1);
	  experiment(initcog, mode, lowsList, readWidth, -1);
	  experiment(initcog, mode, lowsList, readWidth, -1);
	  experiment(initcog, mode, lowsList, readWidth, -1);

		experiment(initcog, mode, lowsList, readWidth, 100);
		experiment(initcog, mode, lowsList, readWidth, 100);
		experiment(initcog, mode, lowsList, readWidth, 100);
		experiment(initcog, mode, lowsList, readWidth, 100);
		experiment(initcog, mode, lowsList, readWidth, 100);
	}

	public static void reloadCogValues(ArrayCog src, ArrayCog dest) {
		for (int i=0; i<src.length; i++) {
			dest.keys[i] = src.keys[i];
			dest.values[i] = src.values[i];
		}
	}

	public static void experiment(ArrayCog initcog, Mode mode, List<Long> lowsList, long readWidth, int splayInterval) {
		ArrayCog arraycog = new ArrayCog(initcog.length);	  
		reloadCogValues(initcog, arraycog);
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
				long startreadtime = System.nanoTime();
				driver.scan(low, low + readWidth);
				long endreadtime = System.nanoTime();
				long delta = (endreadtime-startreadtime)/1000;
				timer += delta;
				if((i+1) % splayInterval == 0){
					long key = SplayBST.findMedianKey(driver.root);
					driver.root = SplayBST.splayTheCog(driver.root,key);					
				}    
			}
		} else {
			for(int i = 0; i < lowsList.size(); i++) {
				Long low = lowsList.get(i);
				long startreadtime = System.nanoTime();
				driver.scan(low, low + readWidth);
				long endreadtime = System.nanoTime();
				long delta = (endreadtime-startreadtime)/1000;
				timer += delta;
			}
		}
		//System.out.println("Splay Time : " + splaytimer);
		return timer;
	}


}