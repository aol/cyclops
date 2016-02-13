package com.aol.cyclops.react.stream.traits.operators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;

import org.jooq.lambda.tuple.Tuple2;

public class StreamCopier {
	 public static final <A> Tuple2<Iterator<A>,Iterator<A>> toBufferingDuplicator(Iterator<A> iterator) {
		 return toBufferingDuplicator(iterator,Long.MAX_VALUE);
	 }
	 public static final <A> Tuple2<Iterator<A>,Iterator<A>> toBufferingDuplicator(Iterator<A> iterator,long pos) {
		 LinkedList<A> bufferTo = new LinkedList<A>();
		 LinkedList<A> bufferFrom = new LinkedList<A>();
		  return new Tuple2(new DuplicatingIterator(bufferTo,bufferFrom,iterator,Long.MAX_VALUE,0),
				  new DuplicatingIterator(bufferFrom,bufferTo,iterator,pos,0));
	 }
	 public static final <A> List<Iterator<A>> toBufferingCopier(Iterator<A> iterator,int copies) {
		List<Iterator<A>> result = new ArrayList<>();
		List<CopyingIterator<A>> leaderboard = new LinkedList<>();
		LinkedList<A> buffer = new LinkedList<>();
		 for(int i=0;i<copies;i++)
			 result.add(new CopyingIterator(iterator,leaderboard,buffer,copies));
		 return result;
	 }
	
	 @AllArgsConstructor
	 static class DuplicatingIterator<T> implements Iterator<T>{
		
		 LinkedList<T> bufferTo;
		 LinkedList<T> bufferFrom;
		 Iterator<T> it;
		 long otherLimit=Long.MAX_VALUE;
		 long counter=0;
		 

		@Override
		public boolean hasNext() {
			if(bufferFrom.size()>0 || it.hasNext())
				return true;
			return false;
		}

		@Override
		public T next() {
			try{
				if(bufferFrom.size()>0)
					return bufferFrom.remove(0);
				else{
					T next = it.next();
					if(counter<otherLimit)
						bufferTo.add(next);
					return next;
				}
			}finally{
				counter++;
			}
		}
		 
	 }
	
	 static class CopyingIterator<T> implements Iterator<T>{
		
		 
		 LinkedList<T> buffer;
		 Iterator<T> it;
		 List<CopyingIterator<T>> leaderboard = new LinkedList<>();
		 boolean added=  false;
		 int total = 0;
		 int counter=0;

		@Override
		public boolean hasNext() {
			
			if(isLeader())
				return it.hasNext();
			if(isLast())
				return buffer.size()>0 || it.hasNext();
			if(it.hasNext())
				return true;
			return counter < buffer.size();
		}

		private boolean isLeader() {
			return leaderboard.size()==0 || this==leaderboard.get(0);
		}
		private boolean isLast() {
			return leaderboard.size()==total && this==leaderboard.get(leaderboard.size()-1);
		}

		@Override
		public T next() {
			
			if(!added){
				
				this.leaderboard.add(this);
				added = true;
			}
			
			if(isLeader()){
				
				return handleLeader();
			}
			if(isLast()){
				
				if(buffer.size()>0)
					return buffer.poll();
				return it.next();
			}
			if(counter< buffer.size())	
				return buffer.get(counter++);
			return handleLeader(); //exceed buffer, now leading
				
		 
	 }

		private T handleLeader() {
			T next = it.next();
			buffer.offer(next);
			return next;
		}

		public CopyingIterator(Iterator<T> it,
				List<CopyingIterator<T>> leaderboard,LinkedList<T> buffer,int total) {
			
			this.it = it;
			this.leaderboard = leaderboard;
			this.buffer = buffer;
			this.total = total;
		}
	 }

}
