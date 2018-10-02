package N_Puzzle;

import java.util.List;

public class Test {
	public static void main(String[] args) {
		NPuzzle p = new NPuzzle();	
		p.shuffle(50);  
		
//		p.initTiles();		
		
		p.show();	
		List<NPuzzle> solution;
		solution = p.aStarSolve();
		p.showSolution(solution);
	}

}
