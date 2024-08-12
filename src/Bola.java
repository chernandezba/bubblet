public class Bola {

	private int color;
	//0 = amarillo
	//1 = azul
	//2 = rojo
	//3 = rosa
	//4 = verde

	public int capa;
	//Capa dentro del tablero a la cual pertenece la bola. Si 0, no pertenece a ninguna capa

	public boolean viva;
	//a true si la bola existe en el tablero
	//false si no existe
	
	public Bola() {
    capa=0;
    viva=false;
    }

	public Bola(int c) {
		color=c;
		capa=0;
		viva=true;
	}

	public int getColor () {
		return color;
	}
	
	public void setColor (int c) {
		color=c;
	}

}
