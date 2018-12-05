package ticketingsystem;

/*class Seat {
    private boolean ifFree [];
    Seat(int stationnum){
        ifFree = new boolean[stationnum-1];//默认初始化为false则表示为该时段空，从站1到站2则ifFree[0]为1
    }
}
class Coach {
    private Seat seats[];
    Coach(int seatnum, int stationnum){
        seats = new Seat[seatnum];
        for(int i=0;i<seatnum;i++){
            seats[i] = new Seat(stationnum);
        }
    }
}
class Route {
    private Coach coaches[];
    Route(int coachnum, int seatnum, int stationnum){
        coaches = new Coach[coachnum];
        for(int i=0;i<coachnum;i++){
            coaches[i] = new Coach(seatnum,stationnum);
        }
    }
}*/

import javafx.util.Pair;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {
    //private Route routes[];
    private static AtomicBoolean[][][][] routes;
    private static AtomicInteger[][] ticketNum;
    private static AtomicLong tid = new AtomicLong();

    private static int totalCoach = 0;
    private static int totalSeat = 0;
    private static int totalStation = 0;
    private static int nowCoach = 0;
    private static int nowSeat = 0;
    private static AtomicInteger totalNum;

    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
    /*routes = new Route[routenum];
    for(int i=0;i<routenum;i++){
        routes[i] = new Route(coachnum,seatnum,stationnum);
    }*/
    tid.set(0);
    totalCoach = coachnum;
    totalSeat = seatnum;
    totalStation = stationnum;

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
    ticketNum = new AtomicInteger[routenum][stationnum-1];
    totalNum = new AtomicInteger(coachnum*seatnum);
    for(int k=0;k<routenum;k++){
        for(int i=0;i<stationnum-1;i++){
            ticketNum[k][i] =  totalNum;
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
            if(inquiry(route,departure,arrival)==0)return null;
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
                    for(int tD=tmpD;tD<tmpA;tD++){
                        ticketNum[route-1][tD].getAndDecrement();
                    }
                    result.coach = tmpCoach;
                    result.seat = tmpSeat;
                    return result;
                }
                for(;k>=tmpD;k--){
                    routes[route-1][i][j][k].compareAndSet(true,false);
                }
            }
        }

        result.passenger = passenger;
        result.route = route;
        result.arrival = arrival;
        result.departure = departure;
        result.tid = tid.getAndIncrement();
        return result;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        int result = totalNum.get();
        int tmpD = departure - 1;
        int tmpA = arrival - 1;
        for(int i=tmpD;i<tmpA;i++){
            //System.out.println("the i is"+i);
            if(ticketNum[route-1][i].get()<result)result = ticketNum[route-1][i].get();
        }
        return result;
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        int tmpCoach = ticket.coach;
        int tmpSeat = ticket.seat;
        int tmpD = ticket.departure - 1;
        int tmpA = ticket.arrival - 1;
        for(int i=0;i<tmpCoach;i++){
            for(int j=0;j<tmpSeat;j++){
                for(int k=tmpA-1;k>=tmpD;k--){
                    routes[ticket.route-1][i][j][k].compareAndSet(true,false);
                }
            }
        }
        for(int i=tmpD;i<tmpA;i++){
            ticketNum[ticket.route-1][i].getAndIncrement();
        }
        return true;
    }

    //ToDo

}
