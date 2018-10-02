package N_Puzzle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class NPuzzle {
	
	private class TilePos {		
	    public int x;
		public int y;			
		public TilePos(int x, int y) {			
			this.x = x;
			this.y = y;
		}		
	}
	public static int DIMS = 4;
	private int[][] tiles;
	private int display_width;
	private TilePos blank;
		
	public NPuzzle() {
		tiles = new int[DIMS][DIMS];
		int cnt = 1;
		for(int i = 0; i < DIMS; i++) {
			for(int j = 0; j < DIMS; j++) {
				tiles[i][j]=cnt;
				cnt++;
			}
		}
		display_width=Integer.toString(cnt).length();
		// init blank
		blank = new TilePos(DIMS - 1, DIMS - 1);
		tiles[blank.x][blank.y] = 0;
	}
	

	
	public final static NPuzzle SOLVED=new NPuzzle();
	
	
	public NPuzzle(NPuzzle toClone) {
		this();  // chain to basic init
		for(TilePos p: allTilePos()) { 
			tiles[p.x][p.y] = toClone.tile(p);
		}
		blank = toClone.getBlank();
	}
	
	

	
	
	
	public List<TilePos> allTilePos() {
		ArrayList<TilePos> out = new ArrayList<TilePos>();
		for(int i=0; i<DIMS; i++) {
			for(int j=0; j<DIMS; j++) {
				out.add(new TilePos(i,j));
			}
		}
		return out;
	}
	
	public int tile(TilePos p) {
		return tiles[p.x][p.y];
	}
	
	
	public TilePos getBlank() {
		return blank;
	}
	
	
	public TilePos whereIs(int x) {
		for(TilePos p: allTilePos()) { 
			if( tile(p) == x ) {
				return p;
			}
		}
		return null;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof NPuzzle) {
			for(TilePos p: allTilePos()) { 
				if( this.tile(p) != ((NPuzzle) o).tile(p)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	@Override 
	public int hashCode() {
		int out=0;
		for(TilePos p: allTilePos()) {
			out= (out*DIMS*DIMS) + this.tile(p);
		}
		return out;
	}
	
	public void show() {
		System.out.println("-----------------");
		for(int i=0; i<DIMS; i++) {
			System.out.print("| ");
			for(int j=0; j<DIMS; j++) {
				int n = tiles[i][j];
				String s;
				if( n>0) {
					s = Integer.toString(n);	
				} else {
					s = "";
				}
				while( s.length() < display_width ) {
					s += " ";
				}
				System.out.print(s + "| ");
			}
			System.out.print("\n");
		}
		System.out.print("-----------------\n\n");
	}
	
	public List<TilePos> allValidMoves() {
		ArrayList<TilePos> out = new ArrayList<TilePos>();
		for(int dx=-1; dx<2; dx++) {
			for(int dy=-1; dy<2; dy++) {
				TilePos tp = new TilePos(blank.x + dx, blank.y + dy);
				if( isValidMove(tp) ) {
					out.add(tp);
				}
			}
		}
		return out;
	}
	
	public boolean isValidMove(TilePos p) {
		if( ( p.x < 0) || (p.x >= DIMS) ) {
			return false;
		}
		if( ( p.y < 0) || (p.y >= DIMS) ) {
			return false;
		}
		int dx = blank.x - p.x;
		int dy = blank.y - p.y;
		if( (Math.abs(dx) + Math.abs(dy) != 1 ) || (dx*dy != 0) ) {
			return false;
		}
		return true;
	}
	
	public void move(TilePos p) {
		if( !isValidMove(p) ) {
			throw new RuntimeException("Invalid move");
		}
		assert tiles[blank.x][blank.y]==0;
		tiles[blank.x][blank.y] = tiles[p.x][p.y];
		tiles[p.x][p.y]=0;
		blank = p;
	}
	
	/**
	 * returns a new puzzle with the move applied
	 * @param p
	 * @return
	 */
	public NPuzzle moveClone(TilePos p) {
		NPuzzle out = new NPuzzle(this);
		out.move(p);
		return out;
	}

	
	public void shuffle(int howmany) {
		for(int i=0; i<howmany; i++) {
			List<TilePos> possible = allValidMoves();
			int which =  (int) (Math.random() * possible.size());
			TilePos move = possible.get(which);
			this.move(move);
		}
	}
	
	public void initTiles() {
		tiles[0][0] = 0;
	    tiles[0][1] = 3;
	    tiles[0][2] = 8;
	    tiles[1][0] = 4;
	    tiles[1][1] = 1;
	    tiles[1][2] = 7;
	    tiles[2][0] = 2;
	    tiles[2][1] = 6;
	    tiles[2][2] = 5;
	    blank = new TilePos(0, 0);
    }
	
	
	public int numberMisplacedTiles() {
		int wrong=0;
		for(int i=0; i<DIMS; i++) {
			for(int j=0; j<DIMS; j++) {
				if( (tiles[i][j] >0) && ( tiles[i][j] != SOLVED.tiles[i][j] ) ){
					wrong++;
				}
			}
		}
		return wrong;
	}
	
	
	public boolean isSolved() {
		return numberMisplacedTiles() == 0;
	}
	

	/**
	 * distance heuristic for A*
	 * @return
	 */
	public int estimateError() {
		return this.numberMisplacedTiles();

	}
	
	
	public List<NPuzzle> allAdjacentPuzzles() {
		ArrayList<NPuzzle> out = new ArrayList<NPuzzle>();
		for( TilePos move: allValidMoves() ) {
			out.add( moveClone(move) );
		}
		return out;
	}
	
	
	/**
	 * returns a list of boards if it was able to solve it, or else null
	 * @return
	 */
	public List<NPuzzle> aStarSolve() {
	  	HashMap<NPuzzle,NPuzzle> predecessor = new HashMap<NPuzzle,NPuzzle>();
	  	HashMap<NPuzzle,Integer> depth = new HashMap<NPuzzle,Integer>();
	  	final HashMap<NPuzzle,Integer> score = new HashMap<NPuzzle,Integer>();
	  	Comparator<NPuzzle> comparator = new Comparator<NPuzzle>() {
	  		@Override
	  		public int compare(NPuzzle a, NPuzzle b) {
	  			return score.get(a) - score.get(b);
	  		}
	  	};
	  	PriorityQueue<NPuzzle> toVisit = new PriorityQueue<NPuzzle>(10000,comparator);

	  	predecessor.put(this, null);
	  	depth.put(this,0);
	  	score.put(this, this.estimateError());
	  	toVisit.add(this);
	  	int cnt=0;
	  	while( toVisit.size() > 0) {
	  		NPuzzle candidate = toVisit.remove();
	  		cnt++;
//	  		if( cnt % 10000 == 0) {
//	  			System.out.printf("Considered %,d positions. Queue = %,d\n", cnt, toVisit.size());
//	  		}
	  		if( candidate.isSolved() ) {
//	  			System.out.printf("Solution considered %d boards\n", cnt);
	  			LinkedList<NPuzzle> solution = new LinkedList<NPuzzle>();
	  			NPuzzle backtrace=candidate;
	  			while( backtrace != null ) {
	  				solution.addFirst(backtrace);
	  				backtrace = predecessor.get(backtrace);
	  			}
	  			return solution;
	  		}
	  		for(NPuzzle fp: candidate.allAdjacentPuzzles()) {
	  			if( !predecessor.containsKey(fp) ) {
	  				predecessor.put(fp,candidate);
	  				depth.put(fp, depth.get(candidate)+1);
	  				int estimate = fp.estimateError();
					score.put(fp, depth.get(candidate)+1 + estimate);
	  				// dont' add to p-queue until the metadata is in place that the comparator needs
	  				toVisit.add(fp);
	  			}
	  		}
	  	}
	  	return null;
	}
	
	public static void showSolution(List<NPuzzle> solution) {
		if (solution != null ) {
			System.out.printf("Success!  Solution with %d moves:\n", solution.size());
			for( NPuzzle sp: solution) {
				sp.show();
			}
		} else {
			System.out.println("Did not solve. :(");			
		}
	}
	
	
	
	
	
	

}
