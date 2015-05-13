# Trampoline

Simple standalone trampoline implementation for stackless recursion and simulating co-routines / continuations.

## Stackless recursion

Trampolines can be used to turn tail recursive calls into a more iterative form, lessening the likelihood of StackOverflow errors 

	@Test
	public void trampolineTest(){
		
		assertThat(loop(446198426,10).result(),equalTo(25));
		
	}
	Trampoline<Integer> loop(int times,int sum){
		
		if(times==0)
			return Trampoline.done(sum);
		else
			return Trampoline.more(()->loop(times-1,sum+times));
	}
	

The code above could be further simplified using static imports

	@Test
	public void trampolineTest(){
		
		assertThat(loop(446198426,10).result(),equalTo(25));
		
	}
	Trampoline<Integer> loop(int times,int sum){
		
		if(times==0)
			return done(sum);
		else
			return more(()->loop(times-1,sum+times));
	}
	

## Simulating coroutines and continuations

Trampolines can be used to interleaving the execution of different functions on the same thread, in a generic if ugly way

	List results;
	@Test
	public void coroutine(){
		results = new ArrayList();
		Iterator<String> it = Arrays.asList("hello","world","end").iterator();
		Trampoline[] coroutine = new Trampoline[1];
		coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
		withCoroutine(coroutine[0]);
		
		assertThat(results,equalTo(Arrays.asList(0,"hello",1,"world",2,"end",3,4)));
	}
	
	private Trampoline<Integer> print(Object next, Trampoline trampoline) {
		System.out.println(next);
		results.add(next);
		return trampoline;
	}
	public void withCoroutine(Trampoline coroutine){
		
		for(int i=0;i<5;i++){
				print(i,coroutine);
				if(!coroutine.complete())
					coroutine= coroutine.bounce();
				
		}
		
	} 