package ticketingsystem;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    private static AtomicBoolean[][][][] routes;
    private static AtomicInteger[][][] ticketNum;
    private static AtomicLong tid = new AtomicLong();

    private static int totalCoach = 0;
    private static int totalSeat = 0;
    private static int nowCoach = 0;
    private static int nowSeat = 0;
    private static int totalNum;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
    tid.set(0);
    totalCoach = coachnum;
    totalSeat = seatnum;

    routes = new AtomicBoolean[routenum][][][];
    for(int i=0;i<routenum;i++){
        routes[i] = new AtomicBoolean[coachnum][][];
        for(int j=0;j<coachnum;j++){
            routes[i][j] = new AtomicBoolean[seatnum][];
            for(int k=0;k<seatnum;k++){
                routes[i][j][k] = new AtomicBoolean[stationnum-1];
                for(int l=0;l<stationnum-1;l++){
                    routes[i][j][k][l] = new AtomicBoolean(false);
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
        Ticket result = new Ticket();
        int tmpCoach = nowCoach;
        int tmpSeat = nowSeat;
        int tmpD = departure - 1;
        int tmpA = arrival - 1;
        for(int i=tmpCoach+1;i!=tmpCoach;i++){
            //if(inquiry(route,departure,arrival)==0)return null;
            i = i%totalCoach;
            for(int j=tmpSeat+1;j!=tmpSeat;j++){
                j = j%totalSeat;
                int k = tmpD;
                boolean ifSuccess = true;
                for(;k<tmpA;k++){
                    if(!routes[route-1][i][j][k].compareAndSet(false,true)){
                        ifSuccess = false;
                        break;
                    }
                }
                if(ifSuccess){
                    result.coach = tmpCoach;
                    result.seat = tmpSeat;
                    result.passenger = passenger;
                    result.route = route;
                    result.arrival = arrival;
                    result.departure = departure;
                    result.tid = tid.getAndIncrement();
                    nowCoach = tmpCoach;
                    nowSeat = tmpSeat;
                    return result;
                }
                for(;k>=tmpD;k--){
                    routes[route-1][i][j][k].compareAndSet(true,false);
                }
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        int result = 0;
        int tmpD = departure - 1;
        int tmpA = arrival - 1;
        for(int i=0;i<totalCoach;i++){
            for(int j=0;j<totalSeat;j++){
                boolean ifSuccess = true;
                for(int k = tmpD;k<tmpA;k++){
                    if(routes[route-1][i][j][k].get()){
                        ifSuccess = false;
                        break;
                    }
                }
                if(ifSuccess){
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        int tmpCoach = ticket.coach;
        int tmpSeat = ticket.seat;
        int tmpD = ticket.departure - 1;
        int tmpA = ticket.arrival - 1;

        for(int i=tmpA-1;i>=tmpD;i--){
            routes[ticket.route-1][tmpCoach][tmpSeat][i].compareAndSet(true,false);
        }
        nowCoach = tmpCoach;
        nowSeat = tmpSeat;
        return true;
    }

    //ToDo

}
