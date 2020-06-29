import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BBGraph 
{
	public GraphSet Q;
	public GraphSet G;
	public ArrayList<Tuple<Integer, Integer>> S;
	public ArrayList<Tuple<Integer, Integer>> VMatch;
	public ArrayList<Tuple<Integer, Integer>> EMatch;
	public ArrayList<Integer> EMatch_QueryRecord;
	public ArrayList<MatchGraphSet> M;
	
	public class Node
	{
		public int ID;
		public int outDegree;
		public int inDegree;
		public ArrayList<String> label;
		public ArrayList<Relation> relations;
		
		public Node() {
			
		}
		
		public Node(int id, int outD, int inD)
		{
			this.ID = id;
			this.inDegree = inD;
			this.outDegree = outD;
			this.label = new ArrayList<String>();
			this.relations = new ArrayList<BBGraph.Relation>();
		}
	}
	
	public class Relation
	{
		public int ID;
		public ArrayList<String> label;
		public int startNodeID;
		public int endNodeID;
		
		public Relation()
		{
			
		}
		public Relation(int id, int start, int end)
		{
			this.ID = id;
			this.startNodeID = start;
			this.endNodeID = end;
			this.label = new ArrayList<String>();
		}
	}
	
	public class Tuple<X, Y> 
	{
		public X x; 
		public Y y; 
		public Tuple() 
		{ 

		} 
		public Tuple(X x, Y y) 
		{ 
			this.x = x; 
		    this.y = y; 
		} 
		
		@Override
		public boolean equals(Object o)
		{
	        if (o instanceof Tuple<?,?>){
	            if(((Tuple<?, ?>)o).x.equals(this.x) && ((Tuple<?, ?>)o).y.equals(this.y))
	            {
	            	return true;
	            }
	        }
	        return false;
		}
	} 
	
	public class GraphSet
	{
		public ArrayList<Node> nodeSet;
		public ArrayList<Relation> relationSetArrayList;
		
		public GraphSet()
		{
			nodeSet = new ArrayList<BBGraph.Node>();
			relationSetArrayList = new ArrayList<BBGraph.Relation>();
		}
	}
	
	public class MatchGraphSet
	{
		public ArrayList<Tuple<Integer, Integer>> nodePair;
		public ArrayList<Tuple<Integer, Integer>> relationPair;
		
		public MatchGraphSet()
		{
			nodePair = new ArrayList<BBGraph.Tuple<Integer,Integer>>();
			relationPair = new ArrayList<BBGraph.Tuple<Integer,Integer>>();
		}
	}
	
	
	public ArrayList<MatchGraphSet> BBGraphAlgorithm(Node startNode)
	{
		M = new ArrayList<MatchGraphSet>();
		S = new ArrayList<Tuple<Integer,Integer>>();
		VMatch = new ArrayList<Tuple<Integer,Integer>>();
		EMatch = new ArrayList<Tuple<Integer,Integer>>();
		
		EMatch_QueryRecord = new ArrayList<Integer>();
		
		ArrayList<Node> C_uStart = new ArrayList<Node>();
		Tuple<Integer, Integer> candidateNode;
		
		for(int i = 0; i < G.nodeSet.size(); i++)
		{
			if(MNP(startNode, G.nodeSet.get(i)))
			{
				C_uStart.add(G.nodeSet.get(i));
			}
		}
		
		for(int i = 0; i < C_uStart.size(); i++)
		{
			S.clear();
			VMatch.clear();
			EMatch.clear();
			EMatch_QueryRecord.clear();
			
			candidateNode = new Tuple<Integer, Integer>(startNode.ID, C_uStart.get(i).ID);
			
			S.add(candidateNode);
			VMatch.add(candidateNode);
			Search();
		}
		
		return M;
	}
	
	
	public void Search()
	{
		if(!S.isEmpty())
		{
			Tuple<Integer, Integer> branchTuple;
			branchTuple = S.remove(S.size() - 1);
			
			if(CheckNonMatchRelation(SearchNodeInArray(Q.nodeSet, branchTuple.x)))
			{
				BranchNodes(Q.nodeSet.get(branchTuple.x), G.nodeSet.get(branchTuple.y));
			}
			else
			{
				Search();
			}
		}
		else
		{
			MatchGraphSet matchSet = new MatchGraphSet();
			
			for(Tuple<Integer, Integer> t : VMatch)
			{
				matchSet.nodePair.add(new Tuple<Integer, Integer>((int)t.x, (int)t.y));
			}
			
			for(Tuple<Integer, Integer> t : EMatch)
			{
				matchSet.relationPair.add(new Tuple<Integer, Integer>((int)t.x, (int)t.y));
			}
			
			M.add(matchSet);
		}
	}
	

	public void BranchNodes(Node Q_branchNode, Node G_branchNode)
	{
		ArrayList<Relation> nonMatchRelation = new ArrayList<Relation>();
		ArrayList<Tuple<Relation, ArrayList<Relation>>> Cr = new ArrayList<Tuple<Relation,ArrayList<Relation>>>();
		int k = 0;
		
		for(Relation r : Q_branchNode.relations)
		{
			nonMatchRelation.add(r);
			
			for(Tuple<Integer, Integer> tuple : EMatch)
			{
				if(r.ID == tuple.x)
				{
					nonMatchRelation.remove(nonMatchRelation.size() - 1);
					break;
				}
			}
		}
		
		k = nonMatchRelation.size();
		
		for(Relation r1 : nonMatchRelation)
		{
			ArrayList<Relation> tempRelationsArrayList = new ArrayList<Relation>();
			for(Relation r2 : G_branchNode.relations)
			{
				if(MRP(r1, r2, Q_branchNode, G_branchNode))
				{
					tempRelationsArrayList.add(r2);
				}
			}
			Tuple<Relation, ArrayList<Relation>> tempTuple = new Tuple<Relation, ArrayList<Relation>>(r1, tempRelationsArrayList);
			Cr.add(tempTuple);
		}
		
		MatchRelationShip(1, k, Cr, Q_branchNode, G_branchNode);
	}
	

	public void MatchRelationShip(int i, int k, ArrayList<Tuple<Relation, ArrayList<Relation>>> Cr, Node Q_branchNode, Node G_branchNode)
	{
		ArrayList<Tuple<Integer, Integer>> backTrack_S;
		ArrayList<Tuple<Integer, Integer>> backTrack_VMatch;
		
		Relation r1 = Cr.get(i - 1).x;
		for(Relation r2: Cr.get(i - 1).y)
		{
			backTrack_S = CopyTupleArray(S);
			backTrack_VMatch = CopyTupleArray(VMatch);
			if(Check(r1, r2, Q_branchNode, G_branchNode))
			{
				EMatch.add(new Tuple<Integer, Integer>(r1.ID, r2.ID));
				EMatch_QueryRecord.add(r1.ID);
								
				if(i < k)
				{
					MatchRelationShip(i+1, k, Cr, Q_branchNode, G_branchNode);
				}
				else
				{
					Search();
				}
				EMatch.remove(new Tuple<Integer, Integer>(r1.ID, r2.ID));
				EMatch_QueryRecord.remove(r1.ID);
				S = CopyTupleArray(backTrack_S);
				VMatch = CopyTupleArray(backTrack_VMatch);
			}
		}
	}
	

	public boolean Check(Relation r1, Relation r2, Node n1, Node n2)
	{
		Node v1 = SearchNodeInArray(Q.nodeSet, AnotherEndNode(r1, n1));
		Node v2 = SearchNodeInArray(G.nodeSet, AnotherEndNode(r2, n2));
		
		Tuple<Integer, Integer> t = new Tuple<Integer, Integer>(v1.ID, v2.ID);
		
		//if other end point node already match with v1 or v2
		for(Relation r: n1.relations)
		{
			if(r.ID != r1.ID)
			{
				if(VMatch.contains(new Tuple<Integer, Integer>(AnotherEndNode(r,n1), v2.ID)))
				{
					return false;
				}
			}		
		}
		
		for(Relation r: n2.relations)
		{
			if(r.ID != r2.ID)
			{
				if(VMatch.contains(new Tuple<Integer, Integer>(v1.ID, AnotherEndNode(r,n2))))
				{
					return false;
				}
			}		
		}
		
		if(!VMatch.contains(t))
		{
			if(MNP(v1, v2))
			{
				S.add(t);
				VMatch.add(t);
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
	
//-----------------------------------------------------------------------------------------------------
	
	/* Matching Node Principal
	 */
	public boolean MNP(Node n1, Node n2)
	{
		//check if the label of n1 is subset of n2
		if(!n2.label.containsAll(n1.label))
		{
			return false;
		}
		
		//check total degree
		if(n1.outDegree <= n2.outDegree)
		{
			if(n1.inDegree <= n2.inDegree)
			{
				//check group of edges have same label and direction is subset of edges connect to n2
				Map<String, Integer> n1RelationLabelCount = new HashMap<String, Integer>();
				Map<String, Integer> n2RelationLabelCount = new HashMap<String, Integer>();
				for(Relation r1: n1.relations)
				{
					for(String L: r1.label)
					{
						if(n1RelationLabelCount.containsKey(L))
						{
							n1RelationLabelCount.put(L, n1RelationLabelCount.get(L) + 1);
						}
						else
						{
							n1RelationLabelCount.put(L, 1);
						}
					}
				}
				
				for(Relation r2: n2.relations)
				{
					for(String L: r2.label)
					{
						if(n2RelationLabelCount.containsKey(L))
						{
							n2RelationLabelCount.put(L, n2RelationLabelCount.get(L) + 1);
						}
						else
						{
							n2RelationLabelCount.put(L, 1);
						}
					}
				}
				
				for(String key: n1RelationLabelCount.keySet())
				{
					if(n2RelationLabelCount.containsKey(key))
					{
						if(n1RelationLabelCount.get(key) > n2RelationLabelCount.get(key))
						{
							return false;
						}
					}
					else
					{
						return false;
					}
				}
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/* Matching Relaion Principal. Checking Relation r1 connect to n1 and r2 connect to n2 where (n1,n2) is branchnode
	 */
	public boolean MRP(Relation r1, Relation r2, Node n1, Node n2)
	{
		if(CheckDirection(r1, r2, n1, n2))
		{
			if(r2.label.containsAll(r1.label))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/* check whether the direction of r1 and r2 is the same for branchnode n1 and n2
	 */
	public boolean CheckDirection(Relation r1, Relation r2, Node n1, Node n2)
	{
		if(r1.startNodeID == n1.ID)
		{
			if(r2.startNodeID == n2.ID)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(r2.endNodeID == n2.ID)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	/* Copy tuple array
	 */
	public <T1, T2> ArrayList<Tuple<T1, T2>> CopyTupleArray(ArrayList<Tuple<T1, T2>> arr)
	{
		ArrayList<Tuple<T1, T2>> output = new ArrayList<Tuple<T1, T2>>();
		for(Tuple<T1, T2> t: arr)
		{
			output.add(new Tuple<T1, T2>(t.x, t.y));
		}
		return output;
	}
	
	/* give a relation and a node in one side of the relation. return another side node id
	 */
	public int AnotherEndNode(Relation r, Node n)
	{
		if(r.startNodeID == n.ID)
		{
			return r.endNodeID;
		}
		else if(r.endNodeID == n.ID)
		{
			return r.startNodeID;
		}
		
		return -1;
	}
	
	/* using id to search node
	 */
	public Node SearchNodeInArray(ArrayList<Node> arr, int id)
	{
		for(Node n: arr)
		{
			if(n.ID == id)
			{
				return n;
			}
		}
		
		return null;
	}
	
	
	/* if there is non matched relation in n return true, else return false
	 */
	public boolean CheckNonMatchRelation(Node n)
	{
		for(Relation r: n.relations)
		{
			if(!EMatch_QueryRecord.contains(r.ID))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String [] args)
	{
		BBGraph bbGraph = new BBGraph();
		Node n1 = bbGraph.new Node(0, 0, 1);
		n1.label.add("people");
		
		Relation r1 = bbGraph.new Relation(0, 0, 1);
		r1.label.add("friend");
		n1.relations.add(r1);
		
		Node n2 = bbGraph.new Node(1, 1, 0);
		n2.label.add("people");
		n2.relations.add(r1);
		
		bbGraph.Q = bbGraph.new GraphSet();
		bbGraph.Q.nodeSet.add(n1);
		bbGraph.Q.nodeSet.add(n2);
		bbGraph.Q.relationSetArrayList.add(r1);
		
		
		Node n3 = bbGraph.new Node(0, 0, 2);
		n3.label.add("people");
		
		Relation r2 = bbGraph.new Relation(0, 0, 1);
		r2.label.add("friend");
		n3.relations.add(r2);
		
		Node n4 = bbGraph.new Node(1, 1, 0);
		n4.label.add("people");
		n4.relations.add(r2);
		
		Relation r3 = bbGraph.new Relation(1, 0, 2);
		r3.label.add("friend");
		n3.relations.add(r3);
		
		Node n5 = bbGraph.new Node(2, 1, 1);
		n5.label.add("people");
		n5.relations.add(r3);
		
		Relation r4 = bbGraph.new Relation(2, 2, 3);
		r4.label.add("friend");
		n5.relations.add(r4);
		
		Node n6 = bbGraph.new Node(3, 1, 0);
		n6.label.add("people");
		n6.relations.add(r4);
		
		bbGraph.G = bbGraph.new GraphSet();
		bbGraph.G.nodeSet.add(n3);
		bbGraph.G.nodeSet.add(n4);
		bbGraph.G.nodeSet.add(n5);
		bbGraph.G.nodeSet.add(n6);
		bbGraph.G.relationSetArrayList.add(r2);
		bbGraph.G.relationSetArrayList.add(r3);
		bbGraph.G.relationSetArrayList.add(r4);
		
		ArrayList<MatchGraphSet> testGraphSet = bbGraph.BBGraphAlgorithm(n1);
		System.out.print(testGraphSet.size());		
	}
}

