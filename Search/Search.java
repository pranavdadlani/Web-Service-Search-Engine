/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Search;

import indexing.Indexing;
import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Paths;
import java.nio.file.Files;
import parser.Parser;

/**
 *
 * @author Harshal
 */
public class Search {

    static Indexing indexer;
    static Parser parser;
    static List sortedFinalList;

    public static void main(String args[]) {
        String searchQuery;
        Scanner sc = new Scanner(System.in);

        try {
            indexer = new Indexing();
            parser = new Parser();
            parser.parse();
            indexer.index();

            Connection connect;
            Statement statement = null;
            System.out.println("Done. ");
            System.out.println();

            System.out.println("WEB SERVICE SEARCH ENGINE");
            System.out.println("Enter search query");
            searchQuery = sc.nextLine();
            String[] inputTerm = searchQuery.split(" ");

            Class.forName("com.mysql.jdbc.Driver");

            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/newDB?"
                    + "user=root&password=snow");

            statement = connect.createStatement();

            HashMap<String, Double> finalList = new HashMap();

            for (int k = 0; k < inputTerm.length; k++) {

                String query = "select * from indexx where term='" + inputTerm[k]
                        + "' order by tfidf desc";
                ResultSet resultSet = statement.executeQuery(query);

                //System.out.println("FOR " + inputTerm[k]);
                while (resultSet.next()) {

                    double tfidf = resultSet.getDouble("tfidf");
                    String wsdl = resultSet.getString("wsdl");

                    if (finalList.containsKey(wsdl)) {
                        finalList.put(wsdl, finalList.get(wsdl) + tfidf);
                    } else {
                        finalList.put(wsdl, tfidf);
                    }
                }


                sortedFinalList = new LinkedList(finalList.entrySet());
                Collections.sort(sortedFinalList, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return -1 * ((Comparable) ((Map.Entry) (o1)).getValue())
                                .compareTo(((Map.Entry) (o2)).getValue());
                    }
                });
            }

            System.out.println();

            System.out.println("RESULTS RETURNED");
            System.out.println("--------------------------------------------------------------------------------------");

            for (int idx = 0; idx < sortedFinalList.size(); idx++) {
                String s = (String) sortedFinalList.get(idx).toString();
                String result[] = s.split("=");
                System.out.println("WSDL Url: \t" + result[0]);
                long val = (long) (Double.parseDouble(result[1]) * 10000);
                System.out.println("RANKING SCORE: \t" + (double)val/10000);
                System.out.println("--------------------------------------------------------------------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
