package brett;
/**
 * Position einer Figur
 */
public class Position {
	private final int x;
	private final int y;

	/**
	 * 
	 * @param x
	 *            X-Koordinate
	 * @param y
	 *            Y-Koordinate
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return X-Koordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return Y-Koordinate
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * @return true, wenn gleiche Position, sonst false
	 */
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		
		if(!(o instanceof Position)) {
			return false;
		}
		
		Position other = (Position) o;
		
		return x == other.x && y == other.y;
	}
	
	/**
	 * String-Notation einer Position
	 */
	@Override
	public String toString() {
		return x + "/" + y;
	}
}