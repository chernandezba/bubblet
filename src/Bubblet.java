import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/*

TODO

-hacer cambios en caso que tamanyo tablero sea small

CHANGES

0.6.4:
-Completado codigo tamaño tablero

0.6.3:
-Añadida tamaño tablero

0.6.2:
-Deshabilitada compilación por defecto > MIDP-1.0 (sonidos)

0.6.1:
-Añadida ayuda en todos los idiomas
-Añadido efecto de iluminación de bloques en thread
-Añadido sonido al eliminar bolas (requiere MIDP-1.0)

*/

/*
	Logica de juego

	1 - pantalla principal. Botones jugar, salir, Preferencias, Records
	2 - juego. 
	2.0 - Random tablero
	2.1 Pintar tablero
	2.2 - Escanear tablero, agrupar bolas adyacentes por capas
	2.2.1 - Si no hay fichas, Final! :-)
	2.2.2  Si no hay ninguna capa, fin del juego
	2.3 Movimiento puntero
	2.4 Si pulsa FIRE, 
	2.4.1 Si bola no tiene capa, nada
	2.4.2 Si bola tiene capa, iluminar toda la capa
	2.4.2.1 Si vuelve a pulsar FIRE, eliminar bolas de la capa. ir 2.2
	2.4.2.2 Sino, ir 2.3
	2.5 ir 2.3


*/



public final class Bubblet extends MIDlet implements CommandListener {
	
	public static final int MAX_ANCHO_TABLERO = 10;
	public static final int MAX_ALTO_TABLERO = 10;

	public static final int BUBBLET_STANDARD=0;
	public static final int BUBBLET_CONTINUOUS=1;
	public static final int BUBBLET_SHIFTER=2;
	public static final int BUBBLET_MEGASHIFT=3;

	private int ancho_tablero=10;
	private int alto_tablero=7;

	private final Command playCommand, exitCommand , prefCommand, hiscoresCommand,ayudaCommand;

	private Command prefVolverCommand;
	private Command recordsVolverCommand;	
	private Command ayudaVolverCommand;

	private ChoiceGroup prefIdioma;
	private ChoiceGroup prefTipoJuego;
	private ChoiceGroup prefTamanyoJuego;

	public Command finJuegoCommand;

	private final Form mainForm;
	public Display display;

	public SSCanvas pantallaJuego;

	public Thread juegoThread;

	//Datos a guardar
	public Idioma idioma;
	public int tipoJuego=BUBBLET_STANDARD;
	public int recordStandard,totalPuntosStandard,totalPartidasStandard;
	public int recordContinuous,totalPuntosContinuous,totalPartidasContinuous;
	public int recordShifter,totalPuntosShifter,totalPartidasShifter;
	public int recordMegashift,totalPuntosMegashift,totalPartidasMegashift;
	public int tamanyoTablero;  //0=Pequeño, 1=Normal, 2=Grande

	//Datos del tipo de partida actual
	public int record,totalPuntos,totalPartidas;

	private RecordStore rs = null;

	public Bubblet() {
	

		display = Display.getDisplay(this);

		cargaDatos();

		mainForm = new Form("Bubblet");

		StringItem saludo = new StringItem("","Bubblet V.0.6.4.\nCesar H.B. 10/06/2009");
		mainForm.append(saludo);

		playCommand = new Command(idioma.getString("Jugar"), Command.OK, 1);
		mainForm.addCommand(playCommand);

		prefCommand = new Command("Prefs", Command.OK, 1);
		mainForm.addCommand(prefCommand);

		hiscoresCommand = new Command(idioma.getString("Records"), Command.OK, 1);
		mainForm.addCommand(hiscoresCommand);

		ayudaCommand = new Command(idioma.getString("Ayuda"), Command.OK, 1);
		mainForm.addCommand(ayudaCommand);

		exitCommand = new Command(idioma.getString("Salir"), Command.EXIT, 1);
		mainForm.addCommand(exitCommand);


		mainForm.setCommandListener(this);
		display.setCurrent(mainForm);
	}
    
	protected void startApp() {
		display.setCurrent(mainForm);
	}
    
    protected void pauseApp() {
        
    }
    
    protected void destroyApp(boolean unconditional) {
		guardaDatos();
    }
    
	public void commandAction(Command c, Displayable d) {

		if (c == exitCommand) {
			destroyApp(false);
			notifyDestroyed();
		}

		if (c == playCommand) {

			finJuegoCommand = new Command(idioma.getString("Fin"),Command.EXIT,2);

			pantallaJuego=new SSCanvas(ancho_tablero,alto_tablero,tipoJuego);

			pantallaJuego.display=display;

			//Poner datos grabados
			pantallaJuego.record=record;
			pantallaJuego.idioma=idioma;
			pantallaJuego.totalPuntos=totalPuntos;
			pantallaJuego.totalPartidas=totalPartidas;

			pantallaJuego.tamanyoTablero=tamanyoTablero;
			
			//Repetimos la llamada a la funcion pues se ha establecido el margen inicial
			//sin saber previamente el tamanyo del tablero
			pantallaJuego.setMargenesTablero();


			pantallaJuego.addCommand(finJuegoCommand);

			pantallaJuego.setCommandListener(this);

			display.setCurrent(pantallaJuego);

			//Iniciamos el thread
			juegoThread = new Thread(pantallaJuego);
			juegoThread.start();


		}

		if (c == finJuegoCommand) {
			pantallaJuego.detiene();
			record=pantallaJuego.record;
			totalPuntos=pantallaJuego.totalPuntos;
			totalPartidas=pantallaJuego.totalPartidas;


			display.setCurrent(mainForm);
		}

		if (c == prefCommand) {
			eligePreferencias();
			
		}


		if (c == prefVolverCommand) {
			//System.out.println(prefIdioma);
			//System.out.println(prefIdioma.getSelectedIndex());
			idioma.setIdioma(prefIdioma.getSelectedIndex());
			tipoJuego=prefTipoJuego.getSelectedIndex();
			tamanyoTablero=prefTamanyoJuego.getSelectedIndex();
			getRecords();
			display.setCurrent(mainForm);
		}



		if (c == hiscoresCommand) {
			setRecords();
			muestraRecords();
			
		}

		if (c == recordsVolverCommand) {
			display.setCurrent(mainForm);
		}

		if (c == ayudaCommand) {
			muestraAyuda();
			
		}

		if (c == ayudaVolverCommand) {
			display.setCurrent(mainForm);
		}



	}

	public void muestraRecords()
	{
		Display display2;
		Form mainForm2;

		display2 = Display.getDisplay(this);
		mainForm2 = new Form(idioma.getString("Records"));

		int mediaStandard,mediaContinuous,mediaShifter,mediaMegashift;

		if (totalPartidasStandard==0) 
			mediaStandard=0;
		else mediaStandard=totalPuntosStandard/totalPartidasStandard;

		if (totalPartidasContinuous==0) 
			mediaContinuous=0;
		else mediaContinuous=totalPuntosContinuous/totalPartidasContinuous;

		if (totalPartidasShifter==0) 
			mediaShifter=0;
		else mediaShifter=totalPuntosShifter/totalPartidasShifter;

		if (totalPartidasMegashift==0) 
			mediaMegashift=0;
		else mediaMegashift=totalPuntosMegashift/totalPartidasMegashift;



		StringItem records = new StringItem("",
			"--Standard--\n" + idioma.getString("Record")+": " +recordStandard+" "+
			idioma.getString("Partidas")+": "+totalPartidasStandard+"\n"+
			idioma.getString("Puntos totales")+": "+totalPuntosStandard+ " "+
			idioma.getString("Media")+": "+mediaStandard+"\n"+

			"--Continuous--\n" + idioma.getString("Record")+": " +recordContinuous+" "+
			idioma.getString("Partidas")+": "+totalPartidasContinuous+"\n"+
			idioma.getString("Puntos totales")+": "+totalPuntosContinuous+ " "+
			idioma.getString("Media")+": "+mediaContinuous+"\n"+

			"--Shifter--\n" + idioma.getString("Record")+": " +recordShifter+" "+
			idioma.getString("Partidas")+": "+totalPartidasShifter+"\n"+
			idioma.getString("Puntos totales")+": "+totalPuntosShifter+ " "+
			idioma.getString("Media")+": "+mediaShifter+"\n"+

			"--Megashift--\n" + idioma.getString("Record")+": " +recordMegashift+" "+
			idioma.getString("Partidas")+": "+totalPartidasMegashift+"\n"+
			idioma.getString("Puntos totales")+": "+totalPuntosMegashift+ " "+
			idioma.getString("Media")+": "+mediaMegashift);


		mainForm2.append(records);
		
		recordsVolverCommand = new Command(idioma.getString("Volver"), Command.OK, 1);
		mainForm2.addCommand(recordsVolverCommand);
		
		mainForm2.setCommandListener(this);
		
		
		display2.setCurrent(mainForm2);

	}

	public void muestraAyuda()
	{
		Display display2;
		Form mainForm2;
		StringItem ayuda;

		display2 = Display.getDisplay(this);
		mainForm2 = new Form(idioma.getString("Ayuda"));

		switch (idioma.getIdioma()) {
		case Idioma.BUBBLET_INGLES:
			ayuda = new StringItem("",
"* Objective *\n"+

"Align similar bubbles in order to form large blocks before bursting them.  The more bubbles"+

" connected to each other in the block before they are bursted, the greater number of points you"+
" will receive.\n"+

"* Playing *\n"+
"In order to burst a bubble, the bubble must be connected to other similar bubbles on the"+
" playing board.  If you tap a bubble that is connected, those connected bubbles will be"+
" highlighted.  Tap on your selection again in order to burst the bubbles.  All bubbles above"+
" your bursted block will now fall down. You can see the value of your selected bubble block"+
" by checking your 'Bubble Points' in the top right corner of the screen.\n"+

"* Bubble Bonus *\n"+
"If you manage to leave fewer than 5 unbursted bubbles on the board you will be rewarded with"+
" more points at the conclusion of the game.\n"+

"* Game Style *\n"+

"- standard - the default mode of play where there is a set number of bubbles that will not"+
" increase\n"+

"- continuous - in this style, whenever a vertical set of bubbles has been bursted and the"+
" remaining bubbles have shifted over to the right, a new randomly selected column of bubbles"+
" will pop up on the left, thereby allowing a game to be played for an extended amount of"+ 
"time\n"+

"- shifter - in this style, when a set of bubbles has been bursted, all remaining bubbles to the"+
" top and left will shift down and to the right\n"+

"- megashift - this style is a combination of the shifter and continuous styles in that bubbles"+
" will shift down and to the right while empty columns will fill on the left.\n"

);

		break;
		
		case Idioma.BUBBLET_CASTELLANO:
		ayuda = new StringItem ("",
"* Objetivo *\n"+

"Agrupar bolas del mismo color y generar grandes bloques antes de eliminarlas.  "+
"A mas bolas agrupadas, mas puntos conseguiras.\n"+

"* Juego *\n"+
"Para eliminar una bola, debe estar en contacto con bolas del mismo color en el tablero. "+
"Si marcas una bola que esta en contacto con alguna otra, se destacaran en la pantalla. "+
"Vuelve a marcar las bolas para eliminarlas. Todas las bolas superiores caeran al eliminar el "+
"bloque. Puedes ver la puntuacion del bloque seleccionado antes de eliminarlo mirando la " +
"zona superior derecha de la pantalla.\n"+

"* Bubble Bonus* \n"+
"Si al finalizar la partida quedan menos de 5 bolas en pantalla por eliminar conseguiras "+
"puntos extra.\n"+

"* Tipo de juego *\n"+

"- standard - el modo por defecto. Hay un numero determinado de bolas en pantalla y no aumentan\n"+

"- continuous - en este modo, cada vez que se libere una o mas columnas verticales, apareceran "+
"nuevas columnas de colores y tamaños aleatorios, por la parte izquierda de la pantalla, "+
"permitiendo aumentar el tiempo de juego de la partida\n"+

"- shifter - en este modo, cada vez que se elimina un grupo de bolas, las bolas restantes se "+
"desplazan desde la parte superior izquierda hacia abajo y la derecha\n"+

"- megashift - este estilo es la suma del modo shifter y el continuos, aparecen nuevas columnas "+
"de colores y se desplazan las bolas hacia la derecha y abajo.\n"
);
		break;

		case Idioma.BUBBLET_CATALAN:
		ayuda = new StringItem ("",
"* Objectiu *\n"+

"Agrupar boles del mateix color i generar grans blocs abans d'eliminar-les. "+
"Mentre mes boles agrupis, mes punts conseguiras.\n"+

"* Joc *\n"+
"Per eliminar una bola, ha d'estar en contacte amb boles del mateix color en el taulell. "+
"Si marques una bola que esta en contacte amb alguna altra, es resalta en la pantalla. "+
"Torna a marcar les boles per eliminar-les. Totes les boles superiors cauran al eliminar "+
"el bloc. Pots veure la puntuacio del bloc seleccionat abans d'eliminar-lo mirant la "+
"part superior dreta de la pantalla.\n"+

"* Bubble Bonus* \n"+
"Si al finalitzar la partida queden menys de 5 boles en pantalla per eliminar aconseguiras "+
"punts extra.\n"+

"* Tipus de joc *\n"+

"- standard - el mode per defecte. Hi ha un numero determinat de boles en pantalla i no "+
"augmenten\n"+

"- continuous - en aquest mode, cada vegada que s'alliberi una o mes columnes verticals, "+
"apareixeran noves columnes de colors i tamanys aleatoris, per la part esquerra de la "+
"pantalla, permetent augmentar el temps de joc de la partida\n"+

"- shifter - en aquest mode, cada vegada que s'elimina un grup de boles, les boles restants "+
"es desplacen des de la part superior esquerra cap avall i la dreta\n"+

"- megashift - aquest estil es la suma del mode shifter i el continuos, apareixen noves "+
"columnes de colors i es desplaçen les boles cap a la dreta i avall.\n"
);

		break;

		default:
		ayuda = new StringItem ("","you should never see this....\n");
		break;
		}


		mainForm2.append(ayuda);
		
		ayudaVolverCommand = new Command(idioma.getString("Volver"), Command.OK, 1);
		mainForm2.addCommand(ayudaVolverCommand);
		
		mainForm2.setCommandListener(this);
		
		
		display2.setCurrent(mainForm2);

	}


	public void eligePreferencias()
	{


		//Display display;
		Display display2;
		Form mainForm2;
		
		String[] idiomas = {"Castellano","Catala","English"};
		prefIdioma = new ChoiceGroup(idioma.getString("Idioma"),ChoiceGroup.EXCLUSIVE,idiomas,null);
		prefIdioma.setSelectedIndex(idioma.getIdioma(), true) ;

		String[] tiposJuegos = {"Standard","Continuous","Shifter","Megashift"};
		prefTipoJuego = new ChoiceGroup(idioma.getString("Tipo de Juego"),ChoiceGroup.EXCLUSIVE,tiposJuegos,null);
		prefTipoJuego.setSelectedIndex(tipoJuego, true) ;

		String[] tamanyosJuegos = {idioma.getString("Pequenyo"),"Normal",idioma.getString("Grande")};
		prefTamanyoJuego = new ChoiceGroup
			(idioma.getString("Tamanyo Tablero"),ChoiceGroup.EXCLUSIVE,tamanyosJuegos,null);
		prefTamanyoJuego.setSelectedIndex(tamanyoTablero, true) ;
		
		display2 = Display.getDisplay(this);
		mainForm2 = new Form(idioma.getString("Preferencias"));
		mainForm2.append(prefIdioma);
		mainForm2.append(prefTipoJuego);
		mainForm2.append(prefTamanyoJuego);
		
		prefVolverCommand = new Command(idioma.getString("Volver"), Command.OK, 1);
		mainForm2.addCommand(prefVolverCommand);
		
		mainForm2.setCommandListener(this);
		
		
		display2.setCurrent(mainForm2);

	}

	/**
	* Pone las 3 variables generales de records segun el tipo de juego activo
	* Se llama al cargar preferencias y al cambiar tipo de juego
	*/
	public void getRecords()
	{

		//Esto no se deberia cumplir nunca
		if (tipoJuego<BUBBLET_STANDARD || tipoJuego>BUBBLET_MEGASHIFT) tipoJuego=BUBBLET_STANDARD;

		switch (tipoJuego) {
			case BUBBLET_STANDARD:
				record=recordStandard;
				totalPuntos=totalPuntosStandard;
				totalPartidas=totalPartidasStandard;

				break;
			case BUBBLET_CONTINUOUS:
				record=recordContinuous;
				totalPuntos=totalPuntosContinuous;
				totalPartidas=totalPartidasContinuous;

				break;
			case BUBBLET_SHIFTER:
				record=recordShifter;
				totalPuntos=totalPuntosShifter;
				totalPartidas=totalPartidasShifter;

				break;
			case BUBBLET_MEGASHIFT:
				record=recordMegashift;
				totalPuntos=totalPuntosMegashift;
				totalPartidas=totalPartidasMegashift;

				break;
			default:

				break;
	

		}

	}

	/**
	* Pone las 9 variables de records de cada tipo de juego segun el tipo de juego activo
	* Se llama al grabar preferencias y al mostrar records 
	*/
	public void setRecords()
	{
		//Esto no se deberia cumplir nunca
		if (tipoJuego<BUBBLET_STANDARD || tipoJuego>BUBBLET_MEGASHIFT) tipoJuego=BUBBLET_STANDARD;

		switch (tipoJuego) {
			case BUBBLET_STANDARD:
				recordStandard=record;
				totalPuntosStandard=totalPuntos;
				totalPartidasStandard=totalPartidas;

				break;
			case BUBBLET_CONTINUOUS:
				recordContinuous=record;
				totalPuntosContinuous=totalPuntos;
				totalPartidasContinuous=totalPartidas;

				break;
			case BUBBLET_SHIFTER:
				recordShifter=record;
				totalPuntosShifter=totalPuntos;
				totalPartidasShifter=totalPartidas;

				break;
			case BUBBLET_MEGASHIFT:
				recordMegashift=record;
				totalPuntosMegashift=totalPuntos;
				totalPartidasMegashift=totalPartidas;

				break;
			default:

				break;
	

		}

	}

	/**
	* Pone todos los datos que se guardan en disco a 0
	* Los inicializa
	*/
	public void reseteaDatos()
	{
	
	

	tipoJuego=BUBBLET_STANDARD;
	recordStandard=0;
	totalPuntosStandard=0;
	totalPartidasStandard=0;

	recordContinuous=0;
	totalPuntosContinuous=0;
	totalPartidasContinuous=0;

	recordShifter=0;
	totalPuntosShifter=0;
	totalPartidasShifter=0;

	recordMegashift=0;
	totalPuntosMegashift=0;
	totalPartidasMegashift=0;

	tamanyoTablero=1;

	}

	/**
	* Carga las preferencias de usuario, records
	*/
	public void cargaDatos ()
	{
	
	//De momento ponemos todo por defecto

	reseteaDatos();
	int nidioma=2;


    try
    {
      // The second parameter indicates that the record store
      // should be created if it does not exist
      rs = RecordStore.openRecordStore("bubbletdb", false );

	//Hay base de datos. Leemos

	nidioma=readRecord(rs,1);
	tipoJuego=readRecord(rs,2);	

	recordStandard=readRecord(rs,3);
	totalPuntosStandard=readRecord(rs,4);
	totalPartidasStandard=readRecord(rs,5);

	recordContinuous=readRecord(rs,6);
	totalPuntosContinuous=readRecord(rs,7);
	totalPartidasContinuous=readRecord(rs,8);

	recordShifter=readRecord(rs,9);
	totalPuntosShifter=readRecord(rs,10);
	totalPartidasShifter=readRecord(rs,11);

	recordMegashift=readRecord(rs,12);
	totalPuntosMegashift=readRecord(rs,13);
	totalPartidasMegashift=readRecord(rs,14);

	tamanyoTablero=readRecord(rs,15);

    }
    catch (Exception e)
    {
	

	//Error al cargar preferencias. dejaremos todo por defecto	
	reseteaDatos();
	nidioma=2;

    }


	idioma=new Idioma(nidioma);

	//System.out.println("antes de close");

	closeRecStore(rs);
   getRecords();
	}
	
	/**
	* Escribe un registro en la base de datos
	*/
	public void writeRecord(RecordStore r,int n)
	{

		String s;
		s=new String(""+n);

		byte[] rec = s.getBytes();

		try
		{
			r.addRecord(rec, 0, rec.length);
		}
		catch (Exception e)
		{
		}
  }

	/**
	* Lee un registro de la base de datos
	*/
	public int readRecord(RecordStore r,int i)
	{

      byte[] recData = new byte[255]; 
      int len;
		String s;
		int valor;

    try
    {


        if (r.getRecordSize(i) > recData.length)
          recData = new byte[r.getRecordSize(i)];
       
        len = r.getRecord(i, recData, 0);
			s=new String(recData,0,len);
			valor=Integer.parseInt(s);
        //System.out.println("Record #" + i + ": " + s);
        //System.out.println("------------------------------");                        
			return valor;
      
    }
    catch (Exception e)
    {
		return 0;
    }

		
  }


	/**
	* Cierra la base de datos
	*/
	public void closeRecStore(RecordStore r)
	{
		try
		{
		r.closeRecordStore();
		}
		catch (Exception e)
		{
		//System.out.println("excepcion en close");
		}
	}

		
	/**
	* Guarda las preferencias de usuario, records
	*/
	public void guardaDatos()
	{

		RecordStore rs = null;
	
		// Borramos la BD si tenía algo
		try {
			RecordStore.deleteRecordStore("bubbletdb");
		} catch( Exception e ){}
	
	
		try
		{
			// The second parameter indicates that the record store
			// should be created if it does not exist
			rs = RecordStore.openRecordStore("bubbletdb", true );
		}
		catch (Exception e)
		{
		}
	
		setRecords();
		writeRecord(rs,idioma.getIdioma());
		writeRecord(rs,tipoJuego);
	
		writeRecord(rs,recordStandard);
		writeRecord(rs,totalPuntosStandard);
		writeRecord(rs,totalPartidasStandard);
	
		writeRecord(rs,recordContinuous);
		writeRecord(rs,totalPuntosContinuous);
		writeRecord(rs,totalPartidasContinuous);
	
		writeRecord(rs,recordShifter);
		writeRecord(rs,totalPuntosShifter);
		writeRecord(rs,totalPartidasShifter);
	
		writeRecord(rs,recordMegashift);
		writeRecord(rs,totalPuntosMegashift);
		writeRecord(rs,totalPartidasMegashift);

		writeRecord(rs,tamanyoTablero);
	
	
	
		closeRecStore(rs);

	}


}






