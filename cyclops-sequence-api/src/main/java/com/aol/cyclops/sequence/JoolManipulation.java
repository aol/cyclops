package com.aol.cyclops.sequence;

public interface JoolManipulation {
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.removeAll(java.util.stream.Stream)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.removeAll(java.lang.Iterable)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.removeAll(org.jooq.lambda.Seq)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.removeAll(java.lang.Object[])
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.retainAll(java.lang.Iterable)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.retainAll(org.jooq.lambda.Seq)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.retainAll(java.util.stream.Stream)
	public default org.jooq.lambda.Seq org.jooq.lambda.Seq.retainAll(java.lang.Object[])
}
