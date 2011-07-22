package com.netflix.astyanax.thrift;

import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;
import com.netflix.astyanax.Serializer;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

/**
 * Wrapper for a simple list of columns where each column has a scalar value.
 * @author elandau
 *
 * @param <C>
 */
public class ThriftColumnListImpl<C> implements ColumnList<C> {
	private final List<org.apache.cassandra.thrift.Column> columns;
	private HashMap<C, org.apache.cassandra.thrift.Column> lookup;
	private final Serializer<C> colSer;
	
	public ThriftColumnListImpl(List<org.apache.cassandra.thrift.Column> columns, Serializer<C> colSer) {
        Preconditions.checkArgument(columns != null, "Columns must not be null");
        Preconditions.checkArgument(colSer!= null, "Serializer must not be null");
        
		this.colSer = colSer;
		this.columns = columns;
	}

	@Override
	public Iterator<Column<C>> iterator() {
		class IteratorImpl implements Iterator<Column<C>> {
			Iterator<org.apache.cassandra.thrift.Column> base;
			
			public IteratorImpl(Iterator<org.apache.cassandra.thrift.Column> base) {
				this.base = base;
			}
			
			@Override
			public boolean hasNext() {
				return base.hasNext();
			}

			@Override
			public Column<C> next() {
				org.apache.cassandra.thrift.Column c = base.next();
				return new ThriftColumnImpl<C>(
						colSer.fromBytes(c.getName()), 
						c.getValue());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Iterator is immutable");
			}
		}
		return new IteratorImpl(columns.iterator());
	}

	@Override
	public Column<C> getColumnByName(C columnName) {
		if (lookup == null) {
			lookup = new HashMap<C, org.apache.cassandra.thrift.Column>();
			for (org.apache.cassandra.thrift.Column column : columns) {
				lookup.put(colSer.fromBytes(column.getName()), 
						   column);
			}
		}
		
		org.apache.cassandra.thrift.Column c = lookup.get(columnName);
		return new ThriftColumnImpl<C>(
				colSer.fromBytes(c.getName()), 
				c.getValue());
	}

	@Override
	public Column<C> getColumnByIndex(int idx) {
		org.apache.cassandra.thrift.Column c = columns.get(idx);
		return new ThriftColumnImpl<C>(
				colSer.fromBytes(c.getName()), 
				c.getValue());
	}
	
	public C getNameByIndex(int idx) {
		org.apache.cassandra.thrift.Column column = columns.get(idx);
		return colSer.fromBytes(column.getName());
	}

	@Override
	public <C2> Column<C2> getSuperColumn(C columnName, Serializer<C2> colSer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <C2> Column<C2> getSuperColumn(int idx, Serializer<C2> colSer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return columns.isEmpty();
	}

	@Override
	public int size() {
		return columns.size();
	}

	@Override
	public boolean isSuperColumn() {
		return false;
	}

}