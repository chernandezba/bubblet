import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Random;

//Comentar esto para compilar para midp 1.0
//import javax.microedition.media.*;
//import javax.microedition.media.control.*;
//Hasta aqui

public class SSCanvas extends Canvas implements Runnable {

	//Las bolas realmente son de 10, damos 11 para un pixel de separacion
	//static final int ANCHO_BOLA = 11;
	//static final int ALTO_BOLA = 11;


	private int ancho_tablero,alto_tablero;

	private int margenXTablero, margenYTablero;

	//Capa seleccionada
	private int capaActiva;

	//Ultimas bolas seleccionadas
	private int lastBolas=0;

	private int totalCapas;

	private int cursorX,cursorY;
	
	private boolean isRunning;

	private Image imgAmarillo, imgAzul, imgRojo, imgRosa, imgVerde;
	private Image imgAmarilloBig, imgAzulBig, imgRojoBig, imgRosaBig, imgVerdeBig;
	private Image imgCursor;

	//Variables asociadas al estado del thread
	private int estadoThread;
	static final int ESTADO_THREAD_CAPA_MARCADA=1;
	private int colorIncThread,colorRThread,colorGThread,colorBThread;
	static final int INC_COLOR=16;

	public int puntos;

	//Datos guardados
	public int record,totalPuntos,totalPartidas;

	/** Indica si al acabar la partida se ha generado un nuevo record */
	public boolean nuevoRecord;

	public int totalBolas,bubbleBonus;


	public Display display;
	public Command exitCommandFin;
	public Idioma idioma;
	public int tamanyoTablero;  


	public int tipoJuego;

	Bola bolas[][]=new Bola[Bubblet.MAX_ANCHO_TABLERO][Bubblet.MAX_ALTO_TABLERO];

	/** Columna de bolas para el modo continuo/megashift */
	Bola bolasContinua[]=new Bola[Bubblet.MAX_ALTO_TABLERO];

	Random rnd = new Random();

	
	public SSCanvas (int ancho,int alto,int tJuego) {
		isRunning=true;

		ancho_tablero=ancho;
		alto_tablero=alto;
		tipoJuego=tJuego;

		estadoThread=0;

		//Cargar imagenes

		try {
			imgAmarillo = Image.createImage("/amarillo.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgAzul = Image.createImage("/azul.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgRojo = Image.createImage("/rojo.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgRosa = Image.createImage("/rosa.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgVerde = Image.createImage("/verde.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}


		//Cargar cursor
		try {
			imgCursor = Image.createImage("/cursor.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		//Cargar imagenes grandes

		try {
			imgAmarilloBig = Image.createImage("/amarillo_big.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgAzulBig = Image.createImage("/azul_big.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgRojoBig = Image.createImage("/rojo_big.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgRosaBig = Image.createImage("/rosa_big.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}

		try {
			imgVerdeBig = Image.createImage("/verde_big.png");
		} catch (Exception e) {
			System.err.println("error: " + e);
		}



		
		inicializaJuego();


	}


	/**
	* Retorna el ancho de una bola
	* @return ancho
	*/
	public int getAnchoBola()
	{
		if (tamanyoTablero==2) return 20+1;
		else return 10+1;
		
		//damos 1 pixel de separacion
		
	}

	/**
	* Retorna el alto de una bola
	* @return alto
	*/
	public int getAltoBola()
	{
		if (tamanyoTablero==2) return 20+1;
		else return 10+1;
		
		//damos 1 pixel de separacion
		
	}


	/**
	* Retorna valor aleatorio entre 0 y intervalo
	* @param x: intervalo entre 0..x-1
	* @return valor aleatorio
	*/
	public int getRandom(int x)
	{
		int c;
		c=(rnd.nextInt());
		if (c<0) c=-c;
		c=c%x;
		return c;

	}

	public void	setMargenesTablero()
	{
		//Definir margen tablero
		//margenXTablero, margenYTablero
		margenXTablero=(getWidth()/2)-(getAnchoBola()*ancho_tablero)/2;
		//margenYTablero=(getHeight()/2)-(getAltoBola()*alto_tablero)/2;

//System.out.println("inicializaJuego. margenXTablero=  "+margenXTablero+
//		" getWidth="+getWidth()+" getAnchoBola="+getAnchoBola() );


		margenYTablero=20;
	}


	public void inicializaJuego()
	{

		int i,j,c;
		//Bola bolas[][]=new Bola[ancho_tablero][alto_tablero];
 
		for (i=0;i<ancho_tablero;i++) {
			for (j=0;j<alto_tablero;j++) {

				c=getRandom(5);
				bolas[i][j]=new Bola(c);
				//System.out.println(c);
				//bolas[i][j]=new Bola(4);
				
			}
		}

		if (tipoJuego==Bubblet.BUBBLET_CONTINUOUS || tipoJuego==Bubblet.BUBBLET_MEGASHIFT) {
			//Inicializamos la columna de los modos continuos/megashift
			for (j=0;j<alto_tablero;j++) {
				bolasContinua[j]=new Bola(0);
			}
		}


		setMargenesTablero();

		cursorX=0;
		cursorY=0;

		capaActiva=0;

		puntos=0;

		lastBolas=0;

		generaColumnaRandom();


	}

	/** 
	* Dibuja en pantalla una mini-bola para los tipos de juego continuous y megashift
	* @param x: numero de bola. 0....alto_tablero
	*/
	public void pintaMiniBola(Graphics gr,int x,int color,int deltaX)
	{
	//0 = amarillo
	//1 = azul
	//2 = rojo
	//3 = rosa
	//4 = verde

	int r,g,b;

	switch (color) {
		case 0:
			r=232;
			g=232;
			b=0;
		break;
		case 1:
			r=0;
			g=0;
			b=255;
		break;
		case 2:
			r=255;
			g=0;
			b=0;
		break;
		case 3:
			r=255;
			g=0;
			b=255;
		break;
		case 4:
			r=0;
			g=154;
			b=0;
		break;
		default:
			//Esto nunca deberia pasar
			r=0;
			g=0;
			b=0;
		break;
	}
	gr.setColor(r,g,b);

	if (tamanyoTablero==2) gr.fillRect (x*10+deltaX, getHeight()-12, 8, 8);
	else 	gr.fillRect (x*5+deltaX, getHeight()-10, 4, 4);
	}

	/**
	* Pinta las bolas siguientes para los modos continuous/megashift
	*/
	public void pintaMiniBolas(Graphics g)
	{
		int i,x,color;

		Font f = Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

		if (tipoJuego==Bubblet.BUBBLET_CONTINUOUS || tipoJuego==Bubblet.BUBBLET_MEGASHIFT) {
			g.setColor(0,0,0);

			g.drawString (idioma.getString("Siguientes"),0 , getHeight(), Graphics.LEFT | Graphics.BOTTOM);
			for (i=alto_tablero-1,x=0;i>=0;i--,x++) {
				if (bolasContinua[i].viva==false) break;
				color=bolasContinua[i].getColor();
				pintaMiniBola(g,x,color,f.stringWidth(idioma.getString("Siguientes"))+5 );
			}
		}

	}

	public void paint(Graphics g) {

	//Borramos la pantalla
	g.setColor(255,255,255);
	g.fillRect (0, 0, getWidth(), getHeight());

	pintaBolas(g);

	pintaMiniBolas(g);

	//pinta capa activa
//	pintaCapaActiva(g,0,0,0);
	pintaCapaActiva(g,colorRThread,colorGThread,colorBThread);

	//pinta cursor
	pintaCursor(g);

	g.setColor(0,0,0);
	//g.drawString ("ultimasbolas "+lastBolas+" puntos "+puntos,
	//					0 , getHeight(), Graphics.LEFT | Graphics.BOTTOM);

	Font fuente = Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

	g.setFont(fuente);


	if (tamanyoTablero==0) 	{
		g.drawString ("Bub pnts "+lastBolas*(lastBolas-1),	getWidth() , 0, Graphics.RIGHT | Graphics.TOP);
		g.drawString (idioma.getString("Pnts")+" "+puntos,
						0 , 0, Graphics.LEFT | Graphics.TOP);
	}

	else {
		g.drawString ("Bubble points "+lastBolas*(lastBolas-1), getWidth() , 0, Graphics.RIGHT | Graphics.TOP);
		g.drawString (idioma.getString("Puntos")+" "+puntos, 0 , 0, Graphics.LEFT | Graphics.TOP);
	}


	fuente = Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

	g.setFont(fuente);



	//Miramos si no hay ninguna combinacion posible

	//comprobarFinal();

}

	public void comprobarFinal()
	{
		
	//Mirar si no hay ninguna combinacion posible, contar bolas en el panel

		int x,y;
		boolean esFinal;

		totalBolas=0;
		esFinal=true;

		for (x=0;x<ancho_tablero;x++) {
			for (y=0;y<alto_tablero;y++) {
				if (bolas[x][y].viva==true) {
					totalBolas++;
					
					//mirar la bola de la derecha
					if (x!=ancho_tablero-1) {
						if (bolas[x+1][y].viva==true) {
							if (bolas[x][y].getColor()==bolas[x+1][y].getColor() ) {
								esFinal=false;
							}
						}
					}

					//mirar la bola de debajo
					if (y!=alto_tablero-1) {
						if (bolas[x][y+1].viva==true) {
							if (bolas[x][y].getColor()==bolas[x][y+1].getColor() ) {
								esFinal=false;
							}
						}
					}
				}
			}
		}

		if (esFinal==true) {

			sonidoFinJuego();
			
			totalPartidas++;
			if (totalBolas<5) {
				bubbleBonus=(5-totalBolas)*10;
			}
			else bubbleBonus=0;

			puntos +=bubbleBonus;

			if (puntos>record) {
				record=puntos;
				nuevoRecord=true;
			}
			else nuevoRecord=false;

			totalPuntos +=puntos;

			repintarPausa();
			marcadorFinJuego();

			if (bubbleBonus!=0) sonidoBonus();

			inicializaJuego();
		}

	}

	public void apagaBolas() {

		int i,j;

		if (capaActiva==0) return;

		estadoThread=0;

		for (i=0;i<ancho_tablero;i++) {
			for (j=0;j<alto_tablero;j++) {
				bolas[i][j].capa=0;
				capaActiva=0;
			}
		}

		lastBolas=0;

	}
	
	/**
	* Hace caer las bolas verticalmente
	*/
	public void caeBolas() {
    
    int x,y,posy;

    //lo copiamos en una columna temporal
    for (x=ancho_tablero-1;x>=0;x--) {
      posy=alto_tablero-1;
      
      //creamos una columna temporal
      Bola tempBolas[]=new Bola[alto_tablero];
      for (y=alto_tablero-1;y>=0;y--) {
        tempBolas[y]=new Bola();
      }
      
      
      for (y=alto_tablero-1;y>=0;y--) {
        if (bolas[x][y].viva==true) {
          tempBolas[posy]=new Bola (bolas[x][y].getColor());
          posy--;
        }
      }
      //y lo volvemos a copiar a la columna original
      for (y=alto_tablero-1;y>=0;y--) {
        if (tempBolas[y].viva==true) {
        	bolas[x][y].viva=tempBolas[y].viva;
        	bolas[x][y].setColor (tempBolas[y].getColor() );
        }
        else
        {
        	bolas[x][y].viva=false;
        }
        //System.out.println(x);
        //System.out.println(y);
      }
    }
 }

	/**
	* Agrega una columna de bolas en modos continuous y megashift
	*/
	public void agregaColumnaBolas() {
		int i,x;

		//el comportamiento normal es:
		//añadir tantas columnas nuevas como se puedan comenzando por la primera de la izquierda vacia

		//buscar espacio libre
		for (x=0;x<ancho_tablero;x++) {
			if (bolas[x][alto_tablero-1].viva==true) break;
		}

		//no hay espacio libre
		if (x==0) return;
		

		x--;

		//desde la primera libre hasta la izquierda, llenar de columna aleatoria
		for (;x>=0;x--) {
		//if (bolas[x][alto_tablero-1].viva==false) {
			
			//Queremos que se vea cierto sentido de movimiento
			repintarPausa();

			for (i=0;i<alto_tablero;i++) {
				bolas[x][i].setColor(bolasContinua[i].getColor() );
				bolas[x][i].viva=bolasContinua[i].viva;
			}
			//Ya que hemos agregado la columna, generamos una nueva
			generaColumnaRandom();
		//}
		}
	}

	/**
	* Genera una columna aleatoria para los modos continuos y megashift
	*/
	public void generaColumnaRandom() {
		int i,y;	

		//System.out.println("inicio generaColumnaRandom\n");
		if (tipoJuego==Bubblet.BUBBLET_CONTINUOUS || tipoJuego==Bubblet.BUBBLET_MEGASHIFT) {

			//Inicializamos la columna
			for (i=0;i<alto_tablero;i++) bolasContinua[i].viva=false;
			//System.out.println("inicio 2 generaColumnaRandom\n");

			y=getRandom(alto_tablero-1)+1;
			//System.out.println("columna random alto:"+y+"\n");
			//Si alto_tablero=7, random de 0 hasta 5, sumar 1, desde 1 hasta 6
			for (i=alto_tablero-1;y>0;i--,y--) {
				bolasContinua[i].setColor(getRandom(5));
				bolasContinua[i].viva=true;
			}
			

		}

	}

	/**
	* Desplaza todas las bolas superiores en cascada hacia la derecha, en modos shifter y megashift
	*/
	public void shiftBolas() {
		int x,y,x2;
		
		//Queremos que se vea cierto sentido de movimiento
		repintarPausa();
		for (y=0;y<alto_tablero;y++) {
			for (x=ancho_tablero-1;x>0;x--) {
				if (bolas[x][y].viva==false) {
					//Desplazamos la primera bola no vacia de la izquierda a la derecha

					for (x2=x-1;x2>=0;x2--) {
						if (bolas[x2][y].viva==true) break;
					}

					//Este continue deberia saltar al proximo y, en vez de al x
					//pero no pasa nada aun asi
					if (x2==-1) continue;

					bolas[x][y].setColor(bolas[x2][y].getColor());
					bolas[x][y].viva=bolas[x2][y].viva;
					//y ponemos bola vacia
					bolas[x2][y].viva=false;

				}

			}
		}
	}

	/**
	* Desplaza las bolas a la derecha
	*/
	public void juntaBolas() {
    
    
    int x,y,x2;
    
    //Desde la derecha hasta la segunda columna
    for (x=ancho_tablero-1;x>0;x--) {
      if (bolas[x][alto_tablero-1].viva==false) {
          //Desplazamos la primera columna no vacia de la izquierda a la derecha

			//Vamos hasta la primera columna no vacia
			for (x2=x-1;x2>=0;x2--) {
				if (bolas[x2][alto_tablero-1].viva==true) break;
			}

			if (x2==-1) return;
			//no hay mas columnas ocupadas a la izquierda

          for (y=0;y<alto_tablero;y++) {
            bolas[x][y].setColor(bolas[x2][y].getColor());
            bolas[x][y].viva=bolas[x2][y].viva;
            //y ponemos bola vacia
            bolas[x2][y].viva=false;
            //System.out.println(x);
            //System.out.println(y);
          }
      }
    }
            
 }
    


	/**
	* Buscar bolas adyacentes del mismo color, es un metodo recursivo
	*/
	public int recursiveLuz(int x,int y,int color,int b) {


		//Bola no es del mismo color o no hay bola
		if (bolas[x][y].viva==false || bolas[x][y].getColor()!=color ) return b;

		//Ya hemos pasado por esta bola
		if (bolas[x][y].capa == 1) return b;


	//	System.out.println(x);
		//System.out.println(y);
	//	System.out.println(b);

		bolas[x][y].capa=1;
		b++;

		//Ficha superior
		if (y!=0) b=recursiveLuz(x,y-1,color,b);
		//Ficha inferior
		if (y!=alto_tablero-1) b=recursiveLuz(x,y+1,color,b);

		//Ficha izquierda
		if (x!=0) b=recursiveLuz(x-1,y,color,b);
		//Ficha derecha
		if (x!=ancho_tablero-1) b=recursiveLuz(x+1,y,color,b);

		return b;
				
	}

	/**
	* Destruye las bolas marcadas en capa
	*/
	public void destroyBolas() {
		int x,y;

		for (x=0;x<ancho_tablero;x++) {
			for (y=0;y<alto_tablero;y++) {
				if (bolas[x][y].capa==capaActiva) bolas[x][y].viva=false;
			}
		}


	}

	/**
	* Marca las bolas adyacentes del mismo color en una capa
	*/
	public void iluminaBolas() {

		int x,y,b,capa;

		x=cursorX;
		y=cursorY;

		if (bolas[x][y].viva==false) return;

		b=recursiveLuz(x,y,bolas[x][y].getColor(),0);
		if (b>1) {
			capaActiva=1;
			//Creamos el efecto de color
			estadoThread=ESTADO_THREAD_CAPA_MARCADA;
			colorRThread=colorGThread=colorBThread=0;
			colorIncThread=INC_COLOR;
		}

		//Si solo habia una bola, se ha quedado en capa
		else bolas[x][y].capa=0;

		lastBolas=b;

//		System.out.println(x);
	//	System.out.println(y);
		//System.out.println(b);
	
	}

	public void sumaPuntos()
	{
	
	puntos +=lastBolas*(lastBolas-1);

	}

	/**
	* Pinta recuadro alrededor de la capa activa
	*/
	public void pintaCapaActiva(Graphics g,int c_r,int c_g,int c_b) {
		int i,j,c;
		//Bola bolas[][]=new Bola[ancho_tablero][alto_tablero];
 
		if (capaActiva==0) return;

		for (i=0;i<ancho_tablero;i++) {
			for (j=0;j<alto_tablero;j++) {
				if (bolas[i][j].viva==true && bolas[i][j].capa==capaActiva) {
					//pintar recuadro alrededor
					g.setColor(c_r,c_g,c_b);
   				g.drawRect (margenXTablero+i*getAnchoBola(), 
						margenYTablero+j*getAltoBola(), getAnchoBola()-1, getAltoBola()-1);
					g.drawRect (margenXTablero+i*getAnchoBola()+1,
						margenYTablero+j*getAltoBola()+1, getAnchoBola()-3, getAltoBola()-3);
				}
			}
		}
	}

	/**	
	* Muestra el cursor en pantalla
	*/
	public void pintaCursor(Graphics g) {
		g.drawImage (imgCursor, 2+margenXTablero+cursorX*getAnchoBola(), 
						2+margenYTablero+cursorY*getAnchoBola(), Graphics.TOP | Graphics.LEFT);
	}

	/**
	* Dibujar las bolas en pantalla
	*/
	public void pintaBolas(Graphics g) {

		
		int x,y;
		int color_bola;
		int posx,posy;
		Bola b;

		for (x=0;x<ancho_tablero;x++) {
			for (y=0;y<alto_tablero;y++) {
				if (bolas[x][y].viva == true ) {
					color_bola=bolas[x][y].getColor();
					posx=margenXTablero+x*getAnchoBola();
					posy=margenYTablero+y*getAnchoBola();
				
					switch (tamanyoTablero) {
					case 0:
					case 1:
						switch (color_bola) {
						
							case 0:
								g.drawImage (imgAmarillo, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 1:
								g.drawImage (imgAzul, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 2:
								g.drawImage (imgRojo, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 3:
								g.drawImage (imgRosa, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 4:
								g.drawImage (imgVerde, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
						}
					break;

					case 2:
						switch (color_bola) {
						
							case 0:
								g.drawImage (imgAmarilloBig, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 1:
								g.drawImage (imgAzulBig, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 2:
								g.drawImage (imgRojoBig, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 3:
								g.drawImage (imgRosaBig, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
							case 4:
								g.drawImage (imgVerdeBig, posx, posy, Graphics.TOP | Graphics.LEFT);
							break;
						
						}

					break;

					}

				}
			}
		}

	}


	public void run(){
//Pese a que no se dara uso al thread, se deja aqui por si acaso......
	
	while (isRunning == true) {
		while (1==1) {

			if (estadoThread!=0) {
				switch (estadoThread) {
					case ESTADO_THREAD_CAPA_MARCADA:
						repaint();
						serviceRepaints();
						//pintaCapaActiva(this,colorRThread,colorGThread,colorBThread);


						//Creamos un efecto de color para la capa marcada
						colorRThread +=colorIncThread;
						if (colorRThread>255) {
							colorRThread=255;
							colorIncThread=-INC_COLOR;
						}
						else {
							if (colorRThread<0) {
								colorRThread=0;
								colorIncThread=+INC_COLOR;
							} 
						}

						colorGThread=colorRThread;
						colorBThread=colorRThread;
					break;

					default:
						System.out.println("Estado del thread indeterminado");
					break;
				}
			}

//			repaint();
//			serviceRepaints();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}

		}
		}
	}

	/**
	* Marcamos el thread como detenido
	*/
	public void detiene() {

		isRunning=false;
	}


	/**
	* Dar cierto sentido de movimiento al desplazar bolas
	*/
	public void repintarPausa() {

		repintar();
		try{
		  Thread.currentThread().sleep(75);
		} catch(Exception e) {}
	}

	/**
	* Repintar la pantalla
	*/
	public void repintar() {
		repaint();
		serviceRepaints();
	}

	/**
	* Gestion de la pulsacion de teclas
	*/
	public void keyPressed(int keyCode) {

		int action=getGameAction(keyCode);

		switch (action) {

			case FIRE:
			case KEY_NUM5:
            // Disparar
				if (capaActiva==0) {
					iluminaBolas();
					repintar();
				}
				else {

					sonidoDestroyBola();

					//Eliminar las bolas
					destroyBolas();
				
					//Desplazamiento de bolas comun
					caeBolas();

					juntaBolas();

					if (tipoJuego==Bubblet.BUBBLET_CONTINUOUS || tipoJuego==Bubblet.BUBBLET_MEGASHIFT) {
						agregaColumnaBolas();
					}

					if (tipoJuego==Bubblet.BUBBLET_SHIFTER || tipoJuego==Bubblet.BUBBLET_MEGASHIFT) {
						shiftBolas();
					}


					sumaPuntos();
					apagaBolas();
					
					repintar();
					comprobarFinal();
				}
            break;
			case LEFT:
			case KEY_NUM4:
            // Mover a la izquierda
				if (cursorX == 0) cursorX=ancho_tablero-1;
				else cursorX--;

				apagaBolas();
				repintar();				

            break;
			case RIGHT:
			case KEY_NUM6:
            // Mover a la derecha
				if (cursorX == ancho_tablero-1) cursorX=0;
				else cursorX++;

				apagaBolas();
				repintar();				


            break;
			case UP:
			case KEY_NUM2:
            // Mover hacia arriba
				if (cursorY == 0) cursorY=alto_tablero-1;
				else cursorY--;

				apagaBolas();
				repintar();		

            break;
			case DOWN:
			case KEY_NUM8:
            // Mover hacia abajo
				if (cursorY == alto_tablero-1) cursorY=0;
				else cursorY++;
				
				apagaBolas();
				repintar();

            break;
    }
}

/**
* Muestra el marcador de fin de la partida
*/
public void marcadorFinJuego() {

	Command exitCommandF;

 finJuego screenFin;

	exitCommandF = new Command("OK",Command.OK,2);

	screenFin=new finJuego(exitCommandF,this,display);


	//Ponemos los datos a mostrar
	screenFin.puntos=puntos;
	screenFin.record=record;	
	screenFin.totalBolas=totalBolas;
	screenFin.totalPuntos=totalPuntos;
	screenFin.totalPartidas=totalPartidas;
	screenFin.bubbleBonus=bubbleBonus;
	screenFin.idioma=idioma;
	screenFin.nuevoRecord=nuevoRecord;
		
	screenFin.addCommand(exitCommandF);
	screenFin.setCommandListener(screenFin);
	display.setCurrent(screenFin);
 }

/* Compilacion para midp1.0 */

public void sonidoDestroyBola()
{
}
public void sonidoFinJuego()
{
}
public void sonidoBonus()
{
}


/* Fin Compilacion para midp1.0 */



/* Compilacion para midp2.0 */
/*
public void sonidoDestroyBola()
{
	try {
		//nota, duracion, volumen
		Manager.playTone(95, 50, 80);
	} catch (Exception e){}
}

public void sonidoSecuencia(byte[] secuencia)
{
	try{
		Player p = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
		p.realize();
		ToneControl c = (ToneControl)p.getControl("ToneControl");
		c.setSequence(secuencia);
		p.start();
		Thread.sleep(2000);
		p.stop();
	} catch (Exception e)  {}

}

public void sonidoFinJuego()
{

	byte tempo = 100;
	
	byte[] secuencia = {
		ToneControl.VERSION, 1,
		ToneControl.TEMPO, tempo,
		// comienzo del bloque 0
		ToneControl.BLOCK_START, 0, 
		// notas del bloque 0
		90,30, 85,30, 80,30,
		// fin del bloque 0
		ToneControl.BLOCK_END, 0, 
		
		// reproducir bloque 0
		ToneControl.PLAY_BLOCK, 0,
	};

	sonidoSecuencia(secuencia);


}

public void sonidoBonus()
{

	byte tempo = 100;
	
	byte[] secuencia = {
		ToneControl.VERSION, 1,
		ToneControl.TEMPO, tempo,
		// comienzo del bloque 0
		ToneControl.BLOCK_START, 0, 
		// notas del bloque 0
		80,30, 85,30, 90,30,
		// fin del bloque 0
		ToneControl.BLOCK_END, 0, 
		
		// reproducir bloque 0
		ToneControl.PLAY_BLOCK, 0,
	};

	sonidoSecuencia(secuencia);


}

*/
/* Fin Compilacion para midp2.0 */

//Fin clase
}

/** 
* Definicion de la clase de fin de juego
*/
class finJuego extends Canvas implements CommandListener{

	//boton de volver
	Command exitCommandF;
	
	//pantalla
	Canvas pantalla;
	Display display;
	
	//para mostrar los datos en pantalla
	int puntos,record,totalBolas,totalPuntos,totalPartidas,bubbleBonus;
	public boolean nuevoRecord;

	public Idioma idioma;

public finJuego() {
	
}

public finJuego(Command e,Canvas p,Display d) {
	exitCommandF = e;
	pantalla=p;
	display=d;
}

 public void paint(Graphics g) {

	//Borramos pantalla
	Font f=Font.getFont (Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
	int altoLetra=f.getHeight()+1;

	g.setColor(255,255,255);
	g.fillRect (0, 0, getWidth(), getHeight());

	g.setColor(0,0,0);

	g.drawString (idioma.getString("Fin del juego"),
						0 , 0, Graphics.LEFT | Graphics.TOP);

	if (nuevoRecord==true) {
		//g.drawString (idioma.getString("Nuevo Record")+"!!",getWidth(),0, Graphics.RIGHT | Graphics.TOP);
		g.drawString ("!!"+idioma.getString("Nuevo Record")+"!!",0 , altoLetra, Graphics.LEFT | Graphics.TOP);
	}

	g.drawString (idioma.getString("Puntos")+": "+(puntos-bubbleBonus) +
					 " "+idioma.getString("Record")+": "+record,
						0 , altoLetra*2, Graphics.LEFT | Graphics.TOP);

	g.drawString (idioma.getString("Bolas restantes")+": "+totalBolas,
						0 , altoLetra*3, Graphics.LEFT | Graphics.TOP);

	g.drawString ("Bubble bonus: "+bubbleBonus,
						0 , altoLetra*4, Graphics.LEFT | Graphics.TOP);

	g.drawString (idioma.getString("Puntos totales")+": "+puntos,
						0 , altoLetra*5, Graphics.LEFT | Graphics.TOP);

	g.drawString (idioma.getString("Puntuacion media")+": "+(totalPuntos/totalPartidas),
						0 , altoLetra*6, Graphics.LEFT | Graphics.TOP);

	g.drawString (idioma.getString("Partidas totales")+": "+totalPartidas,
						0 , altoLetra*7, Graphics.LEFT | Graphics.TOP);





 }

public void commandAction(Command c, Displayable d) {

		if (c == exitCommandF) {
	
//System.out.println("volver");

		//Si alguien conoce una manera mejor de cerrar el canvas que me lo diga.......
		display.setCurrent(pantalla);


		}


}


}





