import java.io.*;

public class Driver {

	public static void main(String[] args) throws Exception {

		ParserCup p = new ParserCup(new File("yaspl3_input_2.yasp"));
		System.out.println("Parsing effettuato "+ p.parse());
		
		ProcessBuilder processBuilder = new ProcessBuilder();					//COMPILAZIONE DEL .yasp
        // LINUX O MAC OS 
        processBuilder.redirectErrorStream(true);
        processBuilder.command("/usr/bin/clang", "yaspl.c", "-oyaspl.out");

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
                            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
            if(exitCode == 0){
                System.out.println("COMPILE SUCCESS!");
            }else{
                System.out.println("COMPILE ERROR!");

            }
            
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        /*ProcessBuilder pr = new ProcessBuilder();					//AVVIO DEL TERMINALE
        // LINUX O MAC OS 
        pr.redirectErrorStream(true);
        pr.command("/usr/bin/open", "/Applications/Utilities/Terminal.app/").start();*/
	}
}
