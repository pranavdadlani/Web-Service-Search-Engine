package indexing;

/**
 *
 * @author Harshal
 */
import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Indexing {

    static int n;
    static HashMap<String, Double> fileMap[];
    static HashMap<String, Double> allTerms;
    static HashMap<Integer, Double> termFile;
    static String wsdlLink[];

    static HashMap<String, Double> allTermsDup;
    
    public static void index() throws FileNotFoundException, SQLException, ClassNotFoundException {
        
        System.out.println("Indexing...");
        File directory = new File("src/wsdl");
        File files[] = directory.listFiles();
        n = files.length;
        fileMap = new HashMap[n];
        wsdlLink = new String[n];
        allTerms = new HashMap<String,Double>();
        allTermsDup = new HashMap<String,Double>();
        int i = 0;
        for (File file : files) {
            fileMap[i] = new HashMap<String, Double>();
            getTF(file, i);
            i++;

        }
        
        for(Map.Entry<String,Double> pair: allTerms.entrySet()){
                allTermsDup.put(pair.getKey(), Math.log(n/pair.getValue())/Math.log(10));
                
            }

         generateTFIDF();

    }

    public static void getTF(File file, int i) throws FileNotFoundException, SQLException, ClassNotFoundException {
        
        Scanner sc = new Scanner(file);
        int fileCount=0;
        wsdlLink[i] = sc.next();
        
        while (sc.hasNext()) {
            fileCount++;
            String term = sc.next();
            if (fileMap[i].containsKey(term)) {
                fileMap[i].put(term, fileMap[i].get(term) + 1);
            } else {
                fileMap[i].put(term, 1.0);

                if (allTerms.containsKey(term)) {
                    allTerms.put(term, allTerms.get(term) + 1);
                } else {
                    allTerms.put(term, 1.0);
                }

            }
        }
        
        for(Map.Entry<String,Double> pair: fileMap[i].entrySet()){
                fileMap[i].put(pair.getKey(), pair.getValue() / fileCount);
                
            }
        
       

    }
    
    public static void generateTFIDF() throws SQLException, ClassNotFoundException{
        Connection connect;
        Statement statement = null;
       // String s = "hi".
        Class.forName("com.mysql.jdbc.Driver");

            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/newDB?"
                    + "user=root&password=snow");
            
            statement = connect.createStatement();
            statement.execute("truncate indexx");
            
        int id=0;
        
        for(int i=0;i<n;i++){
            
            for(Map.Entry<String,Double> pair: fileMap[i].entrySet()){
                String insertQuery = "insert into indexx values("+(id++) +",'"+pair.getKey()
                        +"',"+(pair.getValue() * allTermsDup.get(pair.getKey()))+",'"+wsdlLink[i]+"');";
                statement.executeUpdate(insertQuery);
                
            }
            
            
        }
        
        
    }

}
