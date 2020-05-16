package net.datatecsolution.admin_tools.modelo.dao;

public class PruebaRetroFitJavaRx {
	private static ApiService mAPIService;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
			
		
		mAPIService = ApiUtils.getApiService();
		
		//mAPIService.getSeccion(1).ObservableList();
		
		
		
		
		
		
		
		
		/*
		mAPIService.getSeccion(1).subscribeOn(Schedulers.computation()).subscribe(
		        new Consumer<Seccion>() {
					public void accept(Seccion incomingNumber) throws Exception {
						System.out.println("incomingNumber " + incomingNumber.toString());
					}

					public void accept(Object arg0) throws Exception {
						// TODO Auto-generated method stub
						
					}
				},
		        new Consumer() {
					@Override
					public void accept(Object error) throws Exception {
						System.out.println("Something went wrong" + ((Throwable)error).getMessage());
					}
				},
		        new Action() {
					@Override
					public void run() throws Exception {
						System.out.println("This observable is finished");
					}
				}
		);*/
		
		/*
		 mAPIService = ApiUtils.getApiService();
		 mAPIService.getSeccion(1).enqueue(new Callback<Seccion>(){

			@Override
			public void onFailure(Call<Seccion> seccion, Throwable arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onResponse(Call<Seccion> seccion, Response<Seccion> response) {
				// TODO Auto-generated method stub
				if(response.isSuccessful()){
					JOptionPane.showMessageDialog(null,response.body().toString());
				}else{
					JOptionPane.showMessageDialog(null, response.code());
				}
				
			}
			 
		 });*/
		//Integer[] numbers = { 0, 1, 2, 3, 4, 5 };
		//Observable numberObservable = Observable.fromArray(numbers);//.from(numbers);

		/*numberObservable.subscribe(
		        new Consumer() {
					public void accept(Seccion incomingNumber) throws Exception {
						System.out.println("incomingNumber " + incomingNumber.toString());
					}

					@Override
					public void accept(Object arg0) throws Exception {
						// TODO Auto-generated method stub
						
					}
				},
		        new Consumer() {
					@Override
					public void accept(Object error) throws Exception {
						System.out.println("Something went wrong" + ((Throwable)error).getMessage());
					}
				},
		        new Action() {
					@Override
					public void run() throws Exception {
						System.out.println("This observable is finished");
					}
				}
		);*/
	
	}

}
