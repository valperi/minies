/**
 * 1. Read the attached CSV file, remove duplicates and after removing duplicates count the number of users.
 * (duplicate users is users that have more than 1 records per minutes)
 * For example: 9999,11:42:52,80,35, 9999,11:42:24,9,99 both records is duplicates – need to count them as 1.
 * (in this case record: 9999,11:42:xx,xx,xx exists 12 times in the file, so the count need to be just 1)
 *
 * 2. Check how many users exist in the polygon (x1,x2,y1,y2) = 10,30,40,60
 * For example record: 9999,10:44:35,12,47 is in this polygon
 * And record 9991,11:30:56,39,55 is not in this polygon
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by valeriar on 09/02/2018.
 */
public class Exam1 {
    private static final Logger LOG = Logger.getLogger(Exam1.class.getName());
    private static final int POLY_X1 = 10;
    private static final int POLY_X2 = 30;
    private static final int POLY_Y1 = 40;
    private static final int POLY_Y2 = 60;
    private static Map<Integer,List<User>> usersMap = new HashMap<>();
    private static Map<Integer,Integer> usersNoDupsCountMap = new HashMap<>();
    private static int usersInPoligonCount = 0;

    public static void main(String[] args) {

        readCSVFile(Exam1.class.getClassLoader().getResource("Exam_locations1.csv").getPath());
        printNumberOfUsers();
        System.out.println("Second assignment printout:");
        System.out.println(String.format("\tNumber of users in Polygon (%s,%s,%s,%s) is %s", POLY_X1, POLY_X2, POLY_Y1, POLY_Y2, usersInPoligonCount));
    }

    private static void readCSVFile(String csvFile) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                /** skip first line in the file**/
                if (i==0) {
                    i++;
                    continue;
                }
                else
                    parseLine(line);
            }

        } catch (FileNotFoundException e) {
            LOG.log(Level.SEVERE, e.toString(), e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.toString(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, e.toString(), e);
                }
            }
        }
    }

    private static void parseLine(String line) {
        String csvSplitBy = ",";
        String[] parsedLine = line.split(csvSplitBy);
        User user = new User(Integer.parseInt(parsedLine[0]), parsedLine[1], Integer.parseInt(parsedLine[2]), Integer.parseInt(parsedLine[3]));
        List<User> newEntry = new ArrayList<>();
        newEntry.add(user);

        usersMap.computeIfPresent(user.getId(), (key, value) -> checkDuplication(user, value));
        usersMap.merge(user.getId(), newEntry, (list1, list2) ->
                Stream.of(list1, list2)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
        usersNoDupsCountMap.putIfAbsent(user.getId(), 1);
        if (isInPolygon(user))
            usersInPoligonCount++;
    }

    private static List<User> checkDuplication(User user, List<User> value) {
        if (!user.isDuplicated(value))
            usersNoDupsCountMap.compute(user.getId(), (i,c) -> c+1);
        return value;
    }

    private static boolean isInPolygon(User user) {
        Integer x = user.getX();
        Integer y = user.getY();
        return ((POLY_X1 <= x) && (x <= POLY_X2) && (POLY_Y1 <= y) && (y <= POLY_Y2));
    }

    private static void printNumberOfUsers() {
        Iterator itAll = usersMap.entrySet().iterator();
        System.out.println("First assignment printout:");
        while (itAll.hasNext()) {
            Map.Entry<Integer, List<User>> entry = (Map.Entry<Integer, List<User>>) itAll.next();
            System.out.println(String.format("\tUser %s has %s entries", entry.getKey().toString(), usersNoDupsCountMap.get(entry.getKey()).toString()));
        }
    }

    public static class User {
        private Integer id;
        private Time ts;
        private Integer x;
        private Integer y;

        public User(Integer id, String ts, Integer x, Integer y) {
            this.id = id;
            this.ts = Time.valueOf(ts);
            this.x = x;
            this.y = y;
        }

        public Integer getId() {
            return id;
        }

        public Time getTs() {
            return ts;
        }

        public Integer getX() {
            return x;
        }

        public Integer getY() {
            return y;
        }

        public boolean isDuplicated(final List<User> entries) {
            boolean duplicated = false;
            for (User user : entries) {
                if (ts.getMinutes() == user.getTs().getMinutes()) {
                    duplicated = true;
                    break;
                }

            }
            return duplicated;
        }
    }
}
