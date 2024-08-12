public class Idioma {

	private int idioma;
	//0 = castellano
	//1 = catalan
	//2 = ingles

	public static final int BUBBLET_CASTELLANO=0;
	public static final int BUBBLET_CATALAN=1;
	public static final int BUBBLET_INGLES=2;

	static int idiomas=3;

	static int nPalabras=23;



	private String palabras[]={
		"Jugar","Jugar","Play",
		"Fin","Fi","End",
		"Fin del juego","Fi del joc","End of game",
		"Salir","Sortir","Exit",
		"Puntos","Punts","Score",
		"Idioma","Llenguatge","Language",
		"Volver","Tornar","Back",
		"Tipo de Juego","Tipus de Joc","Game Type",
		"Records","Records","Hi Scores",
		"Record","Record","Hi Score",
//10

		"Siguientes","Seguents","Next",
		"Preferencias","Preferencies","Preferences",
		"Partidas totales","Partides totals","Total Games",
		"Bolas restantes","Boles restants","Remaining balls",
		"Puntos totales","Punts totals","Total Score",
		"Puntuacion media","Puntuació mitja","Average Score",
		"Partidas","Partides","Games",
		"Media","Mitjana","Average",
		"Nuevo Record","Nou Record","New Hi-score",
		"Ayuda","Ajuda","Help",

//20
		"Tamanyo Tablero","Tamany Tauler","Screen size",
		"Pequenyo","Petit","Small",
		"Grande","Gran","Big"
	
	};

	
	public Idioma() {
    idioma=0;
   }

	public Idioma(int i) {
		setIdioma(i);
	}

	public void setIdioma(int i) {
		if (i<0 || i>2) idioma=0;
		else idioma=i;

	}

	public int getIdioma() {
		return idioma;
	}

	public String getString (String cast) {
		int i,j;

		
		for (i=0,j=0;i<nPalabras;i++,j+=idiomas) {
			if (cast.equals(palabras[j])) {
			//	System.out.println(palabras[j+idioma]);
				return (palabras[j+idioma]);
			}
		}
		System.out.println("Idioma.getString: no hay match para '"+cast+"'\n");
		return cast;
	}
	

}
