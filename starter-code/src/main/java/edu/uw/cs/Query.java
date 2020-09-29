package edu.uw.cs;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.bind.*;

/**
 * Runs queries against a back-end database
 */
public class Query {
  // DB Connection
  private Connection conn;

  private String username;

  // Password hashing parameter constants
  private static final int HASH_STRENGTH = 65536;
  private static final int KEY_LENGTH = 128;

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY =
   " SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;
  // TODO: YOUR CODE HERE


  private static final String UPDATERESERVATIONID_SQL  ="UPDATE Reservation SET rid = ? WHERE rid = ? ";
  private PreparedStatement changeReserStatement;

  private static final String SEARCH_RESERVATION = "SELECT * FROM Reservation as R WHERE R.username = ?";
  private PreparedStatement findReservation;

  private static final String FINDRESFLIGHT_SQL = "SELECT F.day_of_month as Day, "
  + "F.carrier_id as Carrier, F.flight_num as Number, F.fid as fid, "
  + "F.origin_city as Origin, F.dest_city as Destination, "
  + "F.actual_time as Duration, F.capacity as Capacity, F.price as Price "
  + "FROM FLIGHTS as F WHERE F.fid = ?";
private PreparedStatement findReservedFlight;

	private static final String BALANCE_SQL =
			"SELECT balance " 
			+	"FROM Users " 
			+	"WHERE username = ?;";
  protected PreparedStatement getBalanceStatement;
  
  private static final String FINDUSER_SQL = "SELECT U.balance FROM Users as U WHERE U.username = ? ";
  private PreparedStatement searchUserBalanceStatement;
  
  private static final String UPDATE_USER_BALANCE = "UPDATE Users SET balance = ? WHERE username = ?";
  private PreparedStatement updateBalanceStatement;  

	private static final String SEPAIDSTATUS_SQL =
			"UPDATE USERS SET " 
			+		"balance = ? WHERE username = ?;";
	private PreparedStatement setBalanceStatement;

	private static final String PAIDSTATUS_SQL =
			"UPDATE Reservations " 
			+	"SET paid = ? WHERE rid = ?;";
	protected PreparedStatement setPaidStatusStatement;  

  private static final String CHECKAVAILABLESEAT_SQL = 
    "SELECT count(*) "
    + "FROM Reservation as R"
    + "Where R.fid1 = ? or R.fid2 = ?";
  private PreparedStatement findSeatsStatement;

  private static final String ADDRESERVATION_SQL = 
  "INSERT into Reservation values (?, ?, ?, ?, ?, ?, ?) ";
  private PreparedStatement addReservationStatement;


	private static final String PAID_SQL =
			"SELECT * " +
					"FROM Reservations " +
					"WHERE rid = ? and username = ?;";
	protected PreparedStatement PaidStatement;

  private static final String CHECKUSEREXIST_SQL =
   "SELECT * " 
   + "FROM Users " 
   + "WHERE username = ?;";
  private PreparedStatement userExistStatement;  

  private static final String CREATECUSTOMER_SQL = 
  "INSERT INTO Users "
   + "VALUES(?, ?, ?);";
  private PreparedStatement createCustomerStatement;
  
  private static final String SEARCHUSER_SQL = 
  "SELECT U.password as password FROM Users as U Where " 
  + "U.password = ? and U.username = ?"; 
  private PreparedStatement findUserStatement;

  private static final String GETCURRENTRESERVATIONID_SQL = "SELECT rid FROM Reservation";
  private PreparedStatement findReservationStatement;

	private static final String CHECKRESERVETION_SQL =
			"SELECT * FROM Reservations " +
					"WHERE username = ?;";
  protected PreparedStatement checkReservationsStatement;  
  
  private static final String CANCEL_SQL = "SELECT pay, price FROM Reservation as WHERE "
  + "rid = ? AND username = ?";
  private PreparedStatement cancelStatement;

  private static final String CANCELRES_SQL = "DELETE FROM Reservation WHERE reservationId = ? ";
  private PreparedStatement cancelReservationStatement;

  private static final String SEARCHDIRECTFLIGHT_SQL =
  "SELECT TOP (?) F.day_of_month as Day, "
  + "F.carrier_id as Carrier, F.flight_num as Number, F.fid as fid, "
  + "F.origin_city as Origin, F.dest_city as Destination, "
  + "F.actual_time as Duration, F.capacity as Capacity, F.price as Price\n "
  + "FROM FLIGHTS as F "
  + "WHERE F.origin_city = ? AND F.dest_city = ? AND F.day_of_month = ? "
  + "AND F.canceled = 0 "
  + "ORDER BY F.actual_time, F.fid ASC";
  private PreparedStatement findDirectFlightStatement; 
  
  private static final String SEARCHINDIRECTFLIGHT_SQL = "SELECT TOP (?) F1.day_of_month as Day1, "
  		+ "F1.carrier_id as Carrier1, F1.flight_num as Number1, F1.origin_city as Origin1, "
  		+ "F1.dest_city as Destination1, F1.actual_time as Duration1, F1.capacity as Capacity1, "
  		+ "F1.price as Price1, F2.day_of_month as Day2, F2.carrier_id as Carrier2, "
  		+ "F2.flight_num as Number2, F2.origin_city as Origin2, F2.dest_city as Destination2, "
  		+ "F2.actual_time as Duration2, F2.capacity as Capacity2, F2.price as Price2, "
  		+ "F1.fid as fid1, F2.fid as fid2, F1.actual_time + F2.actual_time as Total_time "
  		+ "FROM FLIGHTS as F1, FLIGHTS as F2 "
  		+ "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? "
  		+ "AND F1.day_of_month = ? AND F2.day_of_month = F1.day_of_month AND F1.canceled != 1 " 
  		+ "AND F2.canceled = 0 "
  		+ "ORDER BY Total_time, F1.fid, F2.fid ASC";
  private PreparedStatement findIndirectFlightStatement;

  private static final String CLEARTABLE_SQL = 
  "DELETE FROM Reservation; DELETE FROM Users; ";
  private PreparedStatement clearTableStatement;



  public ArrayList<ArrayList<Flight>> flightSearch;
// THIS IS A NEW COMMENT!!! I'M GOING TO COMMIT IT!!!

  /**
   * Establishes a new application-to-database connection. Uses the
   * dbconn.properties configuration settings
   * 
   * @throws IOException
   * @throws SQLException
   */
  public void openConnection() throws IOException, SQLException {
    // Connect to the database with the provided connection configuration
    Properties configProps = new Properties();
    configProps.load(new FileInputStream("dbconn.properties"));
    String serverURL = configProps.getProperty("hw1.server_url");
    String dbName = configProps.getProperty("hw1.database_name");
    String adminName = configProps.getProperty("hw1.username");
    String password = configProps.getProperty("hw1.password");
    String connectionUrl = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s", serverURL,
        dbName, adminName, password);
    conn = DriverManager.getConnection(connectionUrl);

    // By default, automatically commit after each statement
    conn.setAutoCommit(true);

    // By default, set the transaction isolation level to serializable
    conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
  }

  /**
   * Closes the application-to-database connection
   */
  public void closeConnection() throws SQLException {
    conn.close();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      // TODO: YOUR CODE HERE
      clearTableStatement.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  public void prepareStatements() throws SQLException {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    // TODO: YOUR CODE HERE
    findReservedFlight = conn.prepareStatement(FINDRESFLIGHT_SQL);
    userExistStatement = conn.prepareStatement(CHECKUSEREXIST_SQL);
    createCustomerStatement = conn.prepareStatement(CREATECUSTOMER_SQL);
    findUserStatement = conn.prepareStatement(SEARCHUSER_SQL);
    findDirectFlightStatement = conn.prepareStatement(SEARCHDIRECTFLIGHT_SQL);
    findIndirectFlightStatement = conn.prepareStatement(SEARCHINDIRECTFLIGHT_SQL);
    clearTableStatement = conn.prepareStatement(CLEARTABLE_SQL);
    checkReservationsStatement = conn.prepareStatement(CHECKRESERVETION_SQL);
    findReservationStatement = conn.prepareStatement(GETCURRENTRESERVATIONID_SQL);
    findSeatsStatement = conn.prepareStatement(CHECKAVAILABLESEAT_SQL);
    addReservationStatement = conn.prepareStatement(ADDRESERVATION_SQL);
    changeReserStatement = conn.prepareStatement(UPDATERESERVATIONID_SQL);
    PaidStatement = conn.prepareStatement(PAID_SQL);
    getBalanceStatement = conn.prepareStatement(BALANCE_SQL);
		setPaidStatusStatement = conn.prepareStatement(PAIDSTATUS_SQL);
    setBalanceStatement = conn.prepareStatement(SEPAIDSTATUS_SQL);
    cancelStatement = conn.prepareStatement(CANCEL_SQL);
    cancelReservationStatement = conn.prepareStatement(CANCELRES_SQL);
    searchUserBalanceStatement = conn.prepareStatement(FINDUSER_SQL);
    updateBalanceStatement = conn.prepareStatement(UPDATE_USER_BALANCE);
    findReservation = conn.prepareStatement(SEARCH_RESERVATION);
  }

  
  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username user's username
   * @param password user's password
   *
   * @return If someone has already logged in, then return "User already logged
   *         in\n" For all other errors, return "Login failed\n". Otherwise,
   *         return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password) {
    // TODO: YOUR CODE HERE
    try {
      if (this.username != null) {
        return "User already logged in\n";
      } else {
        findUserStatement.clearParameters();
        findUserStatement.setString(1, password);
        findUserStatement.setString(2, username);
        ResultSet res = findUserStatement.executeQuery();
        if (!res.next()) {
          res.close();
          return "Login failed\n";
        } else {
          this.username = username;
          res.close();
          return "Logged in as " + username + "\n";
        }
      }

    } catch(SQLException E ) {
      return "Login failed\n";
    }
    
  }

  /**
   * Implement the create user function.
   *
   * @param username   new user's username. User names are unique the system.
   * @param password   new user's password.
   * @param initAmount initial amount to deposit into the user's account, should
   *                   be >= 0 (failure otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n"
   *         if failed.
   */
  public String transaction_createCustomer(String username, String password, int initAmount) {
		try {
			if (initAmount < 0) {
				return "Failed to create user\n";
			}
			
			userExistStatement.clearParameters();
			userExistStatement.setString(1, username);
			
			ResultSet res = userExistStatement.executeQuery();
			if (!res.next()) {
        res.close();
        createCustomerStatement.clearParameters();
        createCustomerStatement.setString(1, username);
        createCustomerStatement.setString(2, password);
        createCustomerStatement.setInt(3, initAmount);
        createCustomerStatement.execute();
        return "Created user " + username + "\n";
			}
      return "Failed to create user\n";
		} catch (SQLException e) {
    return "Failed to create user\n";
    }
	}
  

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise is searches for direct flights and
   * flights with two "hops." Only searches for up to the number of itineraries
   * given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight        if true, then only search for direct flights,
   *                            otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
    * @return If no itineraries were found, return "No flights match your
    *         selection\n". If an error occurs, then return "Failed to search\n".
   *
   *         Otherwise, the sorted itineraries printed in the following format:
   *
   *         Itinerary [itinerary number]: [number of flights] flight(s), [total
   *         flight time] minutes\n [first flight in itinerary]\n ... [last flight
   *         in itinerary]\n
   *
   *         Each flight should be printed using the same format as in the
   *         {@code Flight} class. Itinerary numbers in each search should always
   *         start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */



  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
      int numberOfItineraries) {
          boolean flightFound = true;
          StringBuffer sb = new StringBuffer();
          flightSearch = new ArrayList<ArrayList<Flight>>();
          try
          {
            ArrayList<Flight> FlightList = new ArrayList<Flight>();
            findDirectFlightStatement.clearParameters();
            findDirectFlightStatement.setInt(1, numberOfItineraries);
            findDirectFlightStatement.setString(2, originCity);
            findDirectFlightStatement.setString(3, destinationCity);
            findDirectFlightStatement.setInt(4, dayOfMonth);
            ResultSet results = findDirectFlightStatement.executeQuery();
            
            while (results.next()) {
              Flight Itinerary = new Flight();
              Itinerary.capacity = results.getInt("capacity");
              Itinerary.carrierId = results.getString("Carrier");
              Itinerary.dayOfMonth = results.getInt("Day");
              Itinerary.destCity = results.getString("des");
              Itinerary.fid = results.getInt("fid");
              Itinerary.flightNum = results.getString("num");
              Itinerary.originCity = results.getString("org");
              Itinerary.price = results.getInt("Price");
              Itinerary.time = results.getInt("Duration");
              FlightList.add(Itinerary);
              flightFound = false;
       }
            results.close();
            
            if (directFlight) {
              int counter = 0;
              for (Flight s : FlightList) {
                ArrayList<Flight> f1 = new ArrayList<Flight>();
                flightSearch.add(f1);
                flightSearch.get(flightSearch.size() - 1).add(s);
                sb.append("Itinerary " + counter + ": 1 flight(s), " + s.time + " minutes\n");
                sb.append(s.toString() + "\n");         
                counter++;       
              }
              counter = 0;
              
            } else {
              int size = FlightList.size();
              int count = 0;
              findIndirectFlightStatement.clearParameters();
              findIndirectFlightStatement.setInt(1, numberOfItineraries);
              findIndirectFlightStatement.setString(2, originCity);
              findIndirectFlightStatement.setString(3, destinationCity);
              findIndirectFlightStatement.setInt(4, dayOfMonth);
              ResultSet result2 = findIndirectFlightStatement.executeQuery();
              while (result2.next() && numberOfItineraries > size) {
                Flight Itinerary1 = new Flight();
                Itinerary1.capacity = result2.getInt("capacity1");
                Itinerary1.carrierId = result2.getString("Carrier1");
                Itinerary1.dayOfMonth = result2.getInt("Day1");
                Itinerary1.destCity = result2.getString("Destination1");
                Itinerary1.fid = result2.getInt("fid1");
                Itinerary1.flightNum = result2.getString("Number1");
                Itinerary1.originCity = result2.getString("Origin1");
                Itinerary1.price = result2.getInt("Price1");
                Itinerary1.time = result2.getInt("Duration1");


                Flight Itinerary2 = new Flight();
                Itinerary2.capacity = result2.getInt("capacity2");
                Itinerary2.carrierId = result2.getString("Carrier2");
                Itinerary2.dayOfMonth = result2.getInt("Day2");
                Itinerary2.destCity = result2.getString("Destination2");
                Itinerary2.fid = result2.getInt("fid2");
                Itinerary2.flightNum = result2.getString("Number2");
                Itinerary2.originCity = result2.getString("Origin2");
                Itinerary2.price = result2.getInt("Price2");
                Itinerary2.time = result2.getInt("Duration2");
                int totalTime = result2.getInt("Total_time");
                
                while (!FlightList.isEmpty() && totalTime - FlightList.get(0).time >= 0) {
                  sb.append("Itinerary " + count + ": 1 flight(s), " + FlightList.get(0).time + " minutes\n");
                  ArrayList<Flight> f = new ArrayList<Flight>();
                  flightSearch.add(f);
                  if (FlightList.get(0).capacity != 0) {
                    flightSearch.get(flightSearch.size() - 1).add(FlightList.get(0));
                  }
                  count++;
                  sb.append(FlightList.get(0).toString() + "\n");
                  FlightList.remove(0);
                }
                sb.append("Itinerary " + count + ": 2 flight(s), " + totalTime + " minutes\n");
                flightSearch.add(new ArrayList<Flight>());
                if (Itinerary1.capacity != 0 && Itinerary2.capacity != 0) {
                  flightSearch.get(flightSearch.size() - 1).add(Itinerary1);
                  flightSearch.get(flightSearch.size() - 1).add(Itinerary2);
                }
                count++;
                sb.append(Itinerary1.toString() + "\n");
                sb.append(Itinerary2.toString() + "\n");
                size++;
                flightFound = false;
              }
              
              result2.close();
              

              for (Flight s: FlightList) {
                ArrayList<Flight> f2 = new ArrayList<Flight>();
                flightSearch.add(f2);  
                if (s.capacity != 0) {
                  flightSearch.get(flightSearch.size() - 1).add(s);
                }
                sb.append("Itinerary " + count + ": 1 flight(s), " + s.time + " minutes\n");
                sb.append(s.toString() + "\n");
                count++;
              }                    
            }
            if (flightFound) {
              return "No flights match your selection\n";
            }

            return sb.toString();
            
          } catch (SQLException e) {  
            return "Failed to search\n";
          }
          

        
                 
  }
  

  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is
   *                    returned by search in the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations,
   *         not logged in\n". If try to book an itinerary with invalid ID, then
   *         return "No such itinerary {@code itineraryId}\n". If the user already
   *         has a reservation on the same day as the one that they are trying to
   *         book now, then return "You cannot book two flights in the same
   *         day\n". For all other errors, return "Booking failed\n".
   *
   *         And if booking succeeded, return "Booked flight(s), reservation ID:
   *         [reservationId]\n" where reservationId is a unique number in the
   *         reservation system that starts from 1 and increments by 1 each time a
   *         successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId)
  {
	  if (this.username == null) {
		  return "Cannot book reservations, not logged in\n";
	  }
	  if (flightSearch == null || itineraryId - flightSearch.size() > 0|| itineraryId < 0) {
		  return "No such itinerary " + itineraryId + "\n";
	  }
	  if (flightSearch.get(itineraryId).isEmpty()) {
		  return "Booking failed\n";
	  }
	  
	  try {
		  ArrayList<Flight> FList = flightSearch.get(itineraryId);
		  checkReservationsStatement.clearParameters();
		  checkReservationsStatement.setString(1, username);
		  try {
			  ResultSet result = checkReservationsStatement.executeQuery();		
			  if (result.next()) {
				  return "You cannot book two flights in the same day\n";
			  }
		  } catch (SQLException e) {
			  return transaction_book(itineraryId);
		  }
      int ID;
		  findReservationStatement.clearParameters();
		  
		  try {
			  ResultSet reserveid = findReservationStatement.executeQuery();		
			  reserveid.next();
			 ID = reserveid.getInt("reservationID");
		  } catch (SQLException e) {
			  return transaction_book(itineraryId);
		  }
		  Flight f1 = FList.get(0);
		  findSeatsStatement.clearParameters();
		  findSeatsStatement.setInt(1, f1.fid);
		  findSeatsStatement.setInt(2, f1.fid);
		  try {
			  ResultSet availableSeat = findSeatsStatement.executeQuery();
			  availableSeat.next();
		  
			  if (f1.capacity <= availableSeat.getInt(1)) {
				  return "Booking failed\n";
			  }
		  } catch (SQLException e) {
			  return transaction_book(itineraryId);
		  }
		  
		  int fid2 = 0;
		  int f2price = 0;
		  if (FList.size() == 2) {
			  Flight f2 = FList.get(1);
			  findSeatsStatement.clearParameters();
			  findSeatsStatement.setInt(1, f2.fid);
			  findSeatsStatement.setInt(2, f2.fid);
			  try {
				  ResultSet availableSeat = findSeatsStatement.executeQuery();
			  
				  if (f2.capacity - availableSeat.getInt(1) <= 0 || f2.capacity == 0) {
					  return "Booking failed\n";
				  }
			  } catch (SQLException e) {
				  return transaction_book(itineraryId);
			  }
			  fid2 = f2.fid;
			  f2price = f2.price;
			  
		  }  
			  addReservationStatement.clearParameters();
			  addReservationStatement.setString(1, this.username);
			  addReservationStatement.setInt(2, ID);
			  addReservationStatement.setInt(3, f1.dayOfMonth);
			  addReservationStatement.setInt(4, f1.fid);
			  addReservationStatement.setInt(5, fid2);
			  addReservationStatement.setString(6, "false");
			  addReservationStatement.setInt(7, f1.price + f2price);
			  try {
				  addReservationStatement.executeUpdate();
			  } catch (SQLException e) {
				  return transaction_book(itineraryId);
			  }
		  changeReserStatement.clearParameters();
		  changeReserStatement.setInt(1, ID);
		  changeReserStatement.setInt(2, ID - 1);
		  try {
			  changeReserStatement.executeUpdate();
		  } catch (SQLException e) {
			  return transaction_book(itineraryId);
		  }
		  
		  return "Booked flight(s), reservation ID: " + ID + "\n";
	  } catch (SQLException e) {
		  return "Booking failed\n";
	  }	  	  
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n"
   *         If the reservation is not found / not under the logged in user's
   *         name, then return "Cannot find unpaid reservation [reservationId]
   *         under user: [username]\n" If the user does not have enough money in
   *         their account, then return "User has only [balance] in account but
   *         itinerary costs [cost]\n" For all other errors, return "Failed to pay
   *         for reservation [reservationId]\n"
   *
   *         If successful, return "Paid reservation: [reservationId] remaining
   *         balance: [balance]\n" where [balance] is the remaining balance in the
   *         user's account.
   */
  public String transaction_pay(int reservationId) {
      if (this.username == null){
        return "Cannot pay, not logged in\n";
      }
      try {
        PaidStatement.clearParameters();
        PaidStatement.setInt(1,reservationId);
        PaidStatement.setString(2,this.username);
        ResultSet results = PaidStatement.executeQuery();
  
        // if the reservation is not found / not under the logged in user's name
        if (!results.next()){
          return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
        } else {
  
          int paid = results.getInt("paid");
          if (paid == 1){
            results.close();
          
            return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
          } else {
  
          int cost = results.getInt("cost");
          getBalanceStatement.clearParameters();
          getBalanceStatement.setString(1,this.username);
          ResultSet getResBalance = getBalanceStatement.executeQuery();
          getResBalance.next();
          int balance = getResBalance.getInt("balance");
          if (balance - cost > 0) {
            setPaidStatusStatement.clearParameters();
            setPaidStatusStatement.setInt(1, 1);
            setPaidStatusStatement.setInt(2,reservationId);
            setPaidStatusStatement.executeUpdate();
            setBalanceStatement.clearParameters();
            setBalanceStatement.setInt(1, balance - cost);
            setBalanceStatement.setString(2,this.username);
            setBalanceStatement.executeUpdate();
            return "Paid reservation: " + reservationId + " remaining balance: " + (balance - cost) + "\n";
          } else {
            results.close();
            getResBalance.close();
        
        
            return "User has only " + balance + " in account but itinerary costs " + cost + "\n";

          }
        }
      }
    }

        catch (SQLException E) {
        
          return "Failed to pay for reservation " + reservationId + "\n";
        }
    }
  

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not
   *         logged in\n" If the user has no reservations, then return "No
   *         reservations found\n" For all other errors, return "Failed to
   *         retrieve reservations\n"
   *
   *         Otherwise return the reservations in the following format:
   *
   *         Reservation [reservation ID] paid: [true or false]:\n" [flight 1
   *         under the reservation] [flight 2 under the reservation] Reservation
   *         [reservation ID] paid: [true or false]:\n" [flight 1 under the
   *         reservation] [flight 2 under the reservation] ...
   *
   *         Each flight should be printed using the same format as in the
   *         {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations() {
    if (this.username == null) {
		  return "Cannot view reservations, not logged in\n";
	  }
	  try {
		  
		  findReservation.clearParameters();
		  findReservation.setString(1, username);
		  ResultSet reservation = findReservation.executeQuery();
		  StringBuffer sb = new StringBuffer();
		  		  
  		int count = 0;
		  while (reservation.next()) {
			  count = 1;
			  sb.append("Reservation " + reservation.getInt("reservationID") + " paid: " + reservation.getString("pay") + ":\n");
			  findReservedFlight.clearParameters();
			  findReservedFlight.setInt(1, reservation.getInt("fid1"));
			  ResultSet flightInfo = findReservedFlight.executeQuery();
			  flightInfo.next();
        Flight Itinerary = new Flight();
        Itinerary.capacity = flightInfo.getInt("capacity");
        Itinerary.carrierId = flightInfo.getString("Carrier");
        Itinerary.dayOfMonth = flightInfo.getInt("Day");
        Itinerary.destCity = flightInfo.getString("des");
        Itinerary.fid = flightInfo.getInt("fid");
        Itinerary.flightNum = flightInfo.getString("num");
        Itinerary.originCity = flightInfo.getString("org");
        Itinerary.price = flightInfo.getInt("Price");
        Itinerary.time = flightInfo.getInt("Duration");
        

        sb.append(Itinerary.toString() + "\n");
			  if (reservation.getInt("fid2") > 0) {
				  findReservedFlight.clearParameters();
				  findReservedFlight.setInt(1, reservation.getInt("fid1"));
				  ResultSet Info = findReservedFlight.executeQuery();
				  Info.next();
          Flight Itinerary2 = new Flight();
          Itinerary2.capacity = flightInfo.getInt("capacity");
          Itinerary2.carrierId = flightInfo.getString("Carrier");
          Itinerary2.dayOfMonth = flightInfo.getInt("Day");
          Itinerary2.destCity = flightInfo.getString("des");
          Itinerary2.fid = flightInfo.getInt("fid");
          Itinerary2.flightNum = flightInfo.getString("num");
          Itinerary2.originCity = flightInfo.getString("org");
          Itinerary2.price = flightInfo.getInt("Price");
          Itinerary2.time = flightInfo.getInt("Duration");
				  sb.append(Itinerary2.toString() + "\n");
			  }
		  }
		  reservation.close();
		  if (count == 0) {
			  return "No reservations found\n";
		  } else {
		    return sb.toString();
      }
	  } catch (SQLException e) { 
		  return "Failed to retrieve reservations\n";
	  }
	  
    }
  

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   *
   * @return If no user has logged in, then return "Cannot cancel reservations,
   *         not logged in\n" For all other errors, return "Failed to cancel
   *         reservation [reservationId]\n"
   *
   *         If successful, return "Canceled reservation [reservationId]\n"
   *
   *         Even though a reservation has been canceled, its ID should not be
   *         reused by the system.
   */
  public String transaction_cancel(int reservationId) {
    if (this.username == null) {
      return "Cannot cancel reservations, not logged in\n";
  }
  try {
      
      cancelStatement.clearParameters();
      cancelStatement.setInt(1, reservationId);
      cancelStatement.setString(2, this.username);
      ResultSet cancel = cancelStatement.executeQuery();
      if (!cancel.next()) {
        return "Failed to cancel reservation " + reservationId + "\n";
      }
      
      if (cancel.getString("pay").equals("true")) {
        
          // get the current balance of the user
          searchUserBalanceStatement.clearParameters();
          searchUserBalanceStatement.setString(1, this.username);
          ResultSet curr_balance;
          curr_balance = searchUserBalanceStatement.executeQuery();
          curr_balance.next();
          
          // refund to user's balance
          updateBalanceStatement.clearParameters();
          updateBalanceStatement.setInt(1, curr_balance.getInt("balance") + cancel.getInt("price"));
          curr_balance.close();
          updateBalanceStatement.setString(2, this.username);
          try {
            updateBalanceStatement.executeUpdate();
        } catch (SQLException deadlock) {
            return transaction_cancel(reservationId);
        }
          
      }
      
      cancel.close();	
      
      // delete the reservation in reservation table
      cancelReservationStatement.clearParameters();
      cancelReservationStatement.setInt(1, reservationId);
      try {
        cancelReservationStatement.executeUpdate();
  } catch (SQLException deadlock) {
    return transaction_cancel(reservationId);
  }
                 
      return "Canceled reservation " + reservationId + "\n";
             
    } catch (SQLException unknownError) {
      return "Failed to cancel reservation " + reservationId + "\n";    
    }
  
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * A class to store flight information.
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: " + flightNum + " Origin: "
          + originCity + " Dest: " + destCity + " Duration: " + time + " Capacity: " + capacity + " Price: " + price;
    }
  }
}



