import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

// Basado en https://srikarthiks.files.wordpress.com/2019/07/file-transfer-using-tcp.pdf
// Y https://colzlife.com/multi-client-server-program-in-java/
public class Servidor
{	
	public static void main(String[] args) throws Exception
	{	
		//Obtener parametros
		System.out.println("Tamanio del archivo a enviar (100 o 250): ");
		Scanner scanner = new Scanner(System.in);
		int tamEnviar = scanner.nextInt();
		System.out.println("Tamanio del archivo a enviar (100 o 250): ");
		int numClientes = scanner.nextInt();
		scanner.close();
		if(tamEnviar != 100 && tamEnviar != 250)
		{
			Exception e = new Exception("Tamanio especificado no es ni 100 ni 250. Yeet.");
			throw e;
		}
		if(numClientes > 25)
		{
			Exception e = new Exception("Mas de 25 clientes. Yeet.");
			throw e;
		}
		
		//Inicializar Sockets
		ServerSocket serverSocket = new ServerSocket(7071);
		ArrayList<Socket> clientes = new ArrayList<Socket>();
		ArrayList<OutputStream> conexiones = new ArrayList<OutputStream>();
		for(int i = 0; i<numClientes; i++)
		{
			Socket socket = serverSocket.accept();
			clientes.add(socket);
			OutputStream os = socket.getOutputStream();
			conexiones.add(os);
		}
		
		//Especificacion de IP
		InetAddress IA = InetAddress.getByName("localhost");
		
		//Preparacion para carga del archivo
		File archivo = new File("../Test" + tamEnviar + ".txt");
		FileInputStream fis = new FileInputStream(archivo);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] contenido;
		
		//Carga del archivo
		long longitudArchivo = archivo.length();
		long posActual = 0;
		long inicioTemporizador = System.nanoTime();
		while(posActual != longitudArchivo)
		{
			//Define el tamanio de cada paquete (usa un numero default seguro para todos excepto el ultimo que se queda chikito).
			int tamanio = 1400;
			if(longitudArchivo-posActual>=tamanio)
			{
				posActual+=tamanio;
			}
			else
			{
				tamanio = (int)(longitudArchivo-posActual);
				posActual=longitudArchivo;
			}
			contenido = new byte[tamanio];
			bis.read(contenido,0,tamanio);
			for(int i = 0; i<numClientes; i++)
			{
				conexiones.get(i).write(contenido);
			}
			System.out.println("Enviando archivo..." + (posActual*100)/longitudArchivo + "% enviado");
		}
		//Limpieza de OutputStream
		for(int i = 0; i<numClientes; i++)
		{
			conexiones.get(i).flush();
			clientes.get(i).close();
		}
		long finTemporizador = System.nanoTime();
		//Cerrar recursos
		bis.close();
		serverSocket.close();
		System.out.println("Si ves esto es porque lo logramos. Creo. Tiempo de envio: " + (finTemporizador-inicioTemporizador));
	}

}