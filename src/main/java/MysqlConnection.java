import java.sql.*;

public class MysqlConnection {
    private PreparedStatement apartmentInsertStatement;
    private PreparedStatement apartmentExistsStatement;
    private PreparedStatement developerInsertStatement;
    private PreparedStatement developerExistsStatement;
    private PreparedStatement priceInsertStatement;
    private PreparedStatement priceExistsStatement;
    private PreparedStatement logStatement;

    MysqlConnection(String address, String port, String database, String username, String password) {
        try {
            DriverManager.registerDriver(new org.gjt.mm.mysql.Driver());
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + database + "?useUnicode=yes&characterEncoding=UTF-8", username, password);
            apartmentInsertStatement = connection.prepareStatement("INSERT INTO `apartments` (ad_id,rooms, meters, floor, maxfloors, address, description, developer_id, added,city) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?);");
            apartmentExistsStatement = connection.prepareStatement("SELECT COUNT(*) AS apartmentsCount FROM `apartments` WHERE ad_id = ? AND city = ?;");
            developerInsertStatement = connection.prepareStatement("INSERT INTO `developers` (name, added, city) VALUES (?, CURRENT_TIMESTAMP, ?);", Statement.RETURN_GENERATED_KEYS);
            developerExistsStatement = connection.prepareStatement("SELECT id FROM `developers` WHERE name = ? AND city = ?;");
            priceInsertStatement = connection.prepareStatement("INSERT INTO `prices` (apartment_id, price, added, city) VALUES (?, ?, CURRENT_TIMESTAMP, ?);");
            priceExistsStatement = connection.prepareStatement("SELECT COUNT(*) AS pricesCount FROM `prices` WHERE apartment_id = ? AND price = ? AND city = ?;");
            logStatement = connection.prepareStatement("INSERT INTO `logs` (new, updated_price, no_changes, error, date, city) VALUES (?,?,?,?,CURRENT_TIMESTAMP,?);");
            System.out.println("Database connected");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to the database:"+e.getMessage(), e);
        }
    }

    Integer addApartment(Apartment apartment, String city) {
        try {
            Integer developerId;
            developerId = checkDeveloperExists(apartment.getDeveloper(), city);
            if (developerId == null) {
                developerId = addDeveloper(apartment.getDeveloper(), city);
            }
            if (!checkApartmentExists(apartment, city)) {
                apartmentInsertStatement.setInt(1, apartment.getId());
                apartmentInsertStatement.setInt(2, apartment.getRooms());
                apartmentInsertStatement.setDouble(3, apartment.getMeters());
                apartmentInsertStatement.setInt(4, apartment.getFloor());
                apartmentInsertStatement.setInt(5, apartment.getMaxFloors());
                apartmentInsertStatement.setString(6, apartment.getAddress());
                apartmentInsertStatement.setString(7, apartment.getExtendedDescription());
                apartmentInsertStatement.setInt(8, developerId);
                apartmentInsertStatement.setString(9, city);
                apartmentInsertStatement.execute();
                addPrice(apartment, city);
                return 0;
            } else {
                if (!checkPriceExists(apartment, city)) {
                    addPrice(apartment, city);
                    return 1;
                }
            }
            return 2;
        } catch (SQLException e) {
            e.printStackTrace();
            return 3;
        }
    }

    private boolean checkApartmentExists(Apartment apartment, String city) {
        try {
            apartmentExistsStatement.setInt(1, apartment.getId());
            apartmentExistsStatement.setString(2, city);
            ResultSet resultSet = apartmentExistsStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("apartmentsCount") > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Integer checkDeveloperExists(String developer, String city) {
        try {
            developerExistsStatement.setString(1, developer);
            developerExistsStatement.setString(2, city);
            ResultSet resultSet = developerExistsStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        } catch (SQLException e) {
            return null;
        }
    }

    private boolean checkPriceExists(Apartment apartment, String city) {
        try {
            priceExistsStatement.setInt(1, apartment.getId());
            priceExistsStatement.setDouble(2, apartment.getPrice());
            priceExistsStatement.setString(3, city);
            ResultSet resultSet = priceExistsStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("pricesCount") > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Integer addDeveloper(String developer, String city) throws SQLException {
        developerInsertStatement.setString(1, developer);
        developerInsertStatement.setString(2, city);
        developerInsertStatement.execute();
        try (ResultSet generatedKeys = developerInsertStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return -1;
            }
        }
    }

    private void addPrice(Apartment apartment, String city) throws SQLException {
        priceInsertStatement.setInt(1, apartment.getId());
        priceInsertStatement.setDouble(2, apartment.getPrice());
        priceInsertStatement.setString(3, city);
        priceInsertStatement.execute();
    }

    void addLog(Integer[] result, String city) {
        try {
            logStatement.setInt(1, result[0]);
            logStatement.setInt(2, result[1]);
            logStatement.setInt(3, result[2]);
            logStatement.setInt(4, result[3]);
            logStatement.setString(5, city);
            logStatement.execute();
        } catch (SQLException e) {
            System.out.println("Unable to write logs:");
            e.printStackTrace();
        }
    }
}
