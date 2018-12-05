package ticketingsystem;

public class Test {

	public static void main(String[] args) throws InterruptedException {
        
		//final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		//ToDo
		final Integer[] a = {0};
		Thread tt = new Thread(new Runnable() {
			@Override
			public void run() {
			for(int i=0;i<10;i++){
				synchronized (a[0]){
				a[0] = a[0] +1;
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}}
			}
			}
		});
		tt.start();

			while(true){
				synchronized (a[0]){
				System.out.println(a[0]);
				}
				Thread.sleep(1000);
			}

	    
	}
}
