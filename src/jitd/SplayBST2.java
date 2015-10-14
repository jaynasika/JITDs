package jitd;

import java.util.Scanner;

public class SplayBST2  {


	/***************************************************************************
	 * Splay tree function.
	 * **********************************************************************/
	public static Cog splayTheCog(Cog h,long key) {
		if (h == null) return null;
		if (h instanceof BTreeCog) {
			BTreeCog root = (BTreeCog)h;
			int cmp1 = Long.compare(key, root.sep);
			if (cmp1 < 0) {
				// reached leaves in tree, so we're done
				if (root.lhs == null || !(root.lhs instanceof BTreeCog)) {
					return root;
				} else {
					BTreeCog left = (BTreeCog) root.lhs;
					int cmp2 = Long.compare(key, left.sep);
					if (cmp2 < 0) {
						left.lhs = splayTheCog(left.lhs, key);
						h = rotateTheCogToRight(h);
					} else if (cmp2 > 0) {
						left.rhs = splayTheCog(left.rhs, key);
						if (left.rhs != null)
							((BTreeCog)h).lhs = rotateTheCogToLeft(left);
					}

					if (((BTreeCog)h).lhs == null) return h;
					else                return rotateTheCogToRight(h);
				}
			}

			else if (cmp1 > 0) { 
				// reached leaves in tree, so we're done
				if (root.rhs == null || !(root.rhs instanceof BTreeCog)) {
					return root;
				} else {
					BTreeCog right = (BTreeCog) root.rhs;
					int cmp2 = Long.compare(key, right.sep);
					if (cmp2 < 0) {
						right.lhs  = splayTheCog(right.lhs, key);
						if (right.lhs != null)
							((BTreeCog)h).rhs = rotateTheCogToRight(right);
					} else if (cmp2 > 0) {
						right.rhs = splayTheCog(right.rhs, key);
						h = rotateTheCogToLeft(h);
					}

					if (((BTreeCog)h).rhs == null) return h;
					else                 return rotateTheCogToLeft(h);
				}
			}
		}
		return h;
	}

	// right rotate the cog
	private static Cog rotateTheCogToRight(Cog h) {
		if (h instanceof BTreeCog && ((BTreeCog) h).lhs instanceof BTreeCog)  {
			BTreeCog root = (BTreeCog)h;	
			BTreeCog left = (BTreeCog)root.lhs;
			root.lhs = left.rhs;
			left.rhs = root;
			return left;
		}
		return h;
	}


	// left rotate the cog
	private static Cog rotateTheCogToLeft(Cog h) {
		if (h instanceof BTreeCog && ((BTreeCog) h).rhs instanceof BTreeCog) {
			BTreeCog root = (BTreeCog)h;	
			BTreeCog right = (BTreeCog)root.rhs;
			root.rhs = right.lhs;
			right.lhs = root;
			return right;
		}
		return h;
	}

	// test client
	public static void main(String[] args) {
		ArrayCog sixtyarraycog = new ArrayCog(3);
		sixtyarraycog.keys[0] = 55;
		sixtyarraycog.keys[1] = 57;
		sixtyarraycog.keys[2] = 58;
		Cog sixty = new BTreeCog(60, sixtyarraycog, null);

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

		System.out.println("Before splay:\n");
		SplayBST2 splaytest= new SplayBST2();
		splaytest.print("",true,(BTreeCog)cog);
		Scanner in = new Scanner(System.in);
		boolean inLoop = true;
		while(inLoop) {
			System.out.println("Enter the splay element or (Enter -t to terminate)");
			String a = in.nextLine();
			if (!a.equals("-t")) {
				long splayElement = Long.parseLong(a);
				cog = splaytest.splayTheCog(cog, splayElement);
				System.out.println("After splay on " + splayElement + ":\n");
				splaytest.print("",true,cog);
			} else {
				inLoop = false;				
			}
		}
		System.out.println("terminated");
	}
	private void print(String prefix, boolean isTail, Cog cog) {
		if (cog != null) {
			if (cog instanceof BTreeCog) {
				System.out.println(prefix + (isTail ? "└── " : "├── ") + ((BTreeCog)cog).sep);

				print(prefix + (isTail ? "    " : "│   "), false, ((BTreeCog)cog).lhs);
				print(prefix + (isTail ? "    " : "│   "), false, ((BTreeCog)cog).rhs);
			} else if (cog instanceof ArrayCog) {
				System.out.println(prefix + (isTail ? "└── " : "├── ") + ((ArrayCog)cog).toString());			
			} else if (cog instanceof LeafCog) {
				System.out.println(prefix + (isTail ? "└── " : "├── ") + ((LeafCog)cog).toString());
			} else {
				System.out.println(prefix + (isTail ? "└── " : "├── ") + cog.toString());
			}			
		}
	}
}