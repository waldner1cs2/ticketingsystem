package ticketingsystem;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private static boolean[][][][] routes;
    private static AtomicInteger[][][] ticketNum;
    private static AtomicLong tid = new AtomicLong();

    private static int totalCoach = 0;
    private static int totalSeat = 0;
    private static int totalStation = 0;
    private static AtomicInteger nowCoach = new AtomicInteger(0);
    private static int nowSeat = 0;
    private static int totalNum;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
    tid.set(0);
    totalCoach = coachnum;
    totalSeat = seatnum;
    totalStation = stationnum;

    routes = new boolean[routenum][][][];
    for(int i=0;i<routenum;i++){
        routes[i] = new boolean[coachnum][][];
        for(int j=0;j<coachnum;j++){
            routes[i][j] = new boolean[seatnum][];
            for(int k=0;k<seatnum;k++){
                routes[i][j][k] = new boolean[stationnum-1];
                for(int l=0;l<stationnum-1;l++){
                    routes[i][j][k][l] = false;
                }
            }
        }
    }
    totalNum = coachnum*seatnum;

    ticketNum = new AtomicInteger[routenum][stationnum][stationnum];
    for(int i=0;i<routenum;i++){
        for(int j=0;j<stationnum;j++){
            for(int k=0;k<stationnum;k++){
                ticketNum[i][j][k] =  new AtomicInteger(totalNum);
            }
        }// make query faster
    }
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //System.out.println(departure);
        //System.out.println(arrival);
        Ticket result = new Ticket();
        int tmpCoach =new Random().nextInt(totalCoach);
        int tmpSeat = new Random().nextInt(totalSeat);
        int tmpD = departure - 1;
        int tmpA = arrival - 1;
        for(int i=tmpCoach+1;;i++){
            if(inquiry(route,departure,arrival)==0)return null;
            i = i%totalCoach;
            for(int j=tmpSeat+1;;j++){
                j = j%totalSeat;
                int k = tmpD;
                boolean ifSuccess = true;
                synchronized (routes[route-1][i][j]){
                    for(;k<tmpA;k++){
                        if(routes[route-1][i][j][k]){
                            ifSuccess = false;
                            break;
                        }else{
                            routes[route-1][i][j][k] = true;
                        }
                    }
                    if(ifSuccess){
                        int startP = departure-2;
                        int endP = arrival;
                        for(;startP>=0;startP--){
                            if(routes[route-1][i][j][startP]){
                                break;
                            }
                        }
                        for(;endP<totalStation;endP++){
                            //System.out.println("enP:"+endP);
                            if(routes[route-1][i][j][endP-1]){
                                break;
                            }
                        }
                        startP++;
                        endP--;
                        for(;startP<arrival;startP++){
                            int tA = (startP+1)>(departure-1)?(startP+1):(departure-1);
                            for(;tA<=endP;tA++){
                                ticketNum[route-1][startP][tA].getAndDecrement();
                            }
                        }
                        result.coach = i;
                        result.seat = j;
                        result.passenger = passenger;
                        result.route = route;
                        result.arrival = arrival;
                        result.departure = departure;
                        result.tid = tid.getAndIncrement();
                        return result;
                    }
                    for(;k>=tmpD;k--){
                        routes[route-1][i][j][k]=false;
                    }
                }
                if(j == tmpSeat)break;
            }
            if(i == tmpCoach)break;
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return ticketNum[route-1][departure-1][arrival-1].get();
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        int tmpCoach = ticket.coach;
        int tmpSeat = ticket.seat;
        int tmpD = ticket.departure - 1;
        int tmpA = ticket.arrival - 1;
        int tmpR = ticket.route-1;
        synchronized (routes[tmpR][tmpCoach][tmpSeat]){
            for(int i=tmpA-1;i>=tmpD;i--){
                routes[tmpR][tmpCoach][tmpSeat][i]=false;
            }
            int startP = tmpD-1;
            int endP = ticket.arrival;
            for(;startP>=0;startP--){
                if(routes[tmpR][tmpCoach][tmpSeat][startP]){
                    break;
                }
            }
            for(;endP<totalStation;endP++){
                if(routes[tmpR][tmpCoach][tmpSeat][endP-1]){
                    break;
                }
            }
            startP++;
            endP--;
            for(;startP<ticket.arrival;startP++){
                int tA = (startP+1)>tmpD?(startP+1):tmpD;
                for(;tA<=endP;tA++){
                    ticketNum[tmpR][startP][tA].getAndIncrement();
                }
            }
        }

        return true;
    }
}
