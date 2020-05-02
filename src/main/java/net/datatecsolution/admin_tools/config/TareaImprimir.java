package net.datatecsolution.admin_tools.config;

import java.util.Random;

public class TareaImprimir implements Runnable {
	
	private final int tiempoInactividad; // tiempo de inactividad aleatorio para esubproceso
	private final String nombreTarea; // nombre de la tarea
	private final static Random generador = new Random();
	
	public TareaImprimir( String nombre )
	{
		nombreTarea = nombre; // establece el nombre de la tarea
	
		// elige un tiempo de inactividad aleatorio entre 0 y 5 segundos
		tiempoInactividad = generador.nextInt( 5000 ); // milisegundos
	}
	// el método run contiene el código que ejecutará un subproceso
	public void run()
	{
		try // deja el subproceso inactivo durante tiempoInactividad segundos
		{
			System.out.printf( "%s va a estar inactivo durante %d milisegundos.\n",
			nombreTarea, tiempoInactividad );
			Thread.sleep( tiempoInactividad ); // deja el subproceso inactivo
		} // fin de try
		catch ( InterruptedException excepcion )
		{
			System.out.printf( "%s %s\n", nombreTarea,
					"termino en forma prematura, debido a la interrupcion" );
		} // fin de catch

		// imprime el nombre de la tarea
		System.out.printf( "%s termino su inactividad\n", nombreTarea );
	} // fin del método run

}
