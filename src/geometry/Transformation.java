package geometry;


public class Transformation {
	private double[][] matrix;
	private int rows;
	private int cols;
	
	public Transformation() {
		rows = 4;
		cols = 4;
		matrix = new double[rows][cols];
		setIdentity();
	}
	public Transformation(double[][] array) {
		rows = array.length;
		cols = array[0].length;
		matrix = new double[rows][cols];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				matrix[i][j] = array[i][j];
			}
		}
	}
	public Transformation(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		matrix = new double[rows][cols];
	}
	
	public static Transformation identity() {
		return new Transformation();
	}

	private void setIdentity() {
		for(int i = 0; i < rows; i++) {
			matrix[i][i] = 1;
		}
	}
	
	public Transformation multiply(Transformation other) {
		Transformation product = new Transformation(this.getRows(), other.getCols());
		for(int i = 0; i < this.getRows(); i++) {
			for(int j = 0; j < other.getCols(); j++) {
				double sum = 0;
				for(int k = 0; k < this.getCols(); k++) {
					sum += this.matrix[i][k] * other.get(k, j);
				}
				product.set(i, j, sum);
			}
		}
		return product;
	}
	public void set(int i, int j, double sum) {
		matrix[i][j] = sum;
	}
	public double[][] getMatrix(){
		return matrix;
	}
	public double get(int row, int col) {
		return matrix[row][col];
	}
	public int getCols() {
		return cols;
	}
	public int getRows() {
		return rows;
	}
	public Transformation transpose() {
		Transformation trans = new Transformation(cols, rows);
		for(int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				trans.set(i, j, matrix[j][i]);
			}
		}
		return trans;
	}
	public Transformation inverse() {
		Transformation trans = new Transformation(cols, rows);
		double det = this.determinant();
		for(int i = 0; i < cols; i++) {
			for(int j = 0; j < rows; j++) {
				if((i + j) % 2 == 0) {
					trans.set(i, j, this.cofactor(j, i).determinant() / det);
				}
				else {
					trans.set(i, j, -this.cofactor(j, i).determinant() / det);
				}
			}
		}
		return trans;
	}
	public double determinant() {
		if(rows != cols) {
			System.out.println("determinant error ");
			return 0;
		}

		if(rows == 2) {
			return matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0];
		}
		
		double res = 0;
		for(int i = 0; i < cols; i++) {
			if(i % 2 == 0) {
				res += matrix[0][i] * this.cofactor(0, i).determinant();
			}
			else {
				res -= matrix[0][i] * this.cofactor(0, i).determinant();
			}
		}
		return res;
	}
	public Transformation cofactor(int row, int col) {
		Transformation trans = new Transformation(rows - 1, cols - 1);
		for(int i = 0; i < rows - 1; i++) {
			if(i < row) {
				for(int j = 0; j < cols - 1; j++) {
					if(j < col) {
						trans.set(i, j, this.get(i, j));
					}
					else {
						trans.set(i, j, this.get(i, j + 1));
					}
				}
			}
			else {
				for(int j = 0; j < cols - 1; j++) {
					if(j < col) {
						trans.set(i, j, this.get(i + 1, j));
					}
					else {
						trans.set(i, j, this.get(i + 1, j + 1));
					}
				}
			}
		}
		return trans;
	}
	public String toString() {
		String s = "";
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				s += matrix[i][j] + " ";
			}
			s += "\n";
		}
		return s;
		
	}
}
