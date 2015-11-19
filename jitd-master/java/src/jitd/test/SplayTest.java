package jitd.test;

import java.util.Scanner;

import jitd.ArrayCog;
import jitd.BTreeCog;
import jitd.Cog;
import jitd.SplayBST;

public class SplayTest {
	public static void main(String[] args) {
		Cog cog = makeSampleCog();

		System.out.println("Before splay:\n");
		SplayBST.print("",true, cog);
		System.out.println("Current depth : " + SplayBST.getDepth(cog));
		System.out.println("Number of BTree Nodes : " + SplayBST.getBTreeNodesCount(cog));
		System.out.println(SplayBST.findMedianBTreeSeperator(cog, SplayBST.getBTreeNodesCount(cog)));
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		boolean inLoop = true;
		while(inLoop) {
			System.out.println("Enter the splay element or (Enter -t to terminate)");
			String a = in.nextLine();
			if (!a.equals("-t")) {
				long splayElement = Long.parseLong(a);
				cog = SplayBST.splayTheCog(cog, splayElement);
				System.out.println("After splay on " + splayElement + ":\n");
				SplayBST.print("",true,cog);
				System.out.println("Current depth : " + SplayBST.getDepth(cog));
			} else {
				inLoop = false;				
			}
		}
		System.out.println("terminated");		
	}

	private static Cog makeSampleCog() {
		ArrayCog sixtyarraycog = new ArrayCog(3);
		sixtyarraycog.keys[0] = 55;
		sixtyarraycog.keys[1] = 57;
		sixtyarraycog.keys[2] = 58;
		Cog sixty = new BTreeCog(60, sixtyarraycog, null); // Should not be null but used for testing purpose.

		ArrayCog eightyarraycog = new ArrayCog(2);
		eightyarraycog.keys[0] = 87;
		eightyarraycog.keys[1] = 89;
		Cog eighty = new BTreeCog(80, null, eightyarraycog);
		Cog seventy = new BTreeCog(70, sixty, eighty);

		ArrayCog fortyarraycog = new ArrayCog(3);
		fortyarraycog.keys[0] = 35;
		fortyarraycog.keys[1] = 39;
		fortyarraycog.keys[2] = 40;
		Cog fourty = new BTreeCog(40, fortyarraycog, null);

		Cog fifty = new BTreeCog(50, fourty, seventy);

		ArrayCog twentyarraycog = new ArrayCog(3);
		twentyarraycog.keys[0] = 13;
		twentyarraycog.keys[1] = 15;
		twentyarraycog.keys[2] = 17;
		Cog twenty = new BTreeCog(20, twentyarraycog, null);

		Cog thirty = new BTreeCog(30, twenty, fifty);

		ArrayCog hundredarraycog = new ArrayCog(3);
		hundredarraycog.keys[0] = 98;
		hundredarraycog.keys[1] = 99;
		hundredarraycog.keys[2] = 100;
		Cog hundred = new BTreeCog(100, hundredarraycog, null);

		Cog ninety = new BTreeCog(90, thirty, hundred);

		ArrayCog zeroarraycog = new ArrayCog(3);
		zeroarraycog.keys[0] = -1;
		zeroarraycog.keys[1] = -2;
		zeroarraycog.keys[2] = -3;
		Cog zero = new BTreeCog(0, zeroarraycog, null);

		Cog ten = new BTreeCog(10, zero, ninety);

		ArrayCog onetwentyarraycogleft = new ArrayCog(2);
		onetwentyarraycogleft.keys[0] = 114;
		onetwentyarraycogleft.keys[1] = 115;
		ArrayCog onetwentyarraycogright = new ArrayCog(2);
		onetwentyarraycogright.keys[0] = 125;
		onetwentyarraycogright.keys[1] = 126;
		Cog onetwenty = new BTreeCog(120, onetwentyarraycogleft, onetwentyarraycogright);

		Cog cog = new BTreeCog(110, ten, onetwenty);
		return cog;
	}
}
