package org.tapchain.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tapchain.core.Chain.ChainException;
import org.tapchain.core.Chain.ConnectionResultIO;
import org.tapchain.core.Chain.ConnectionResultO;
import org.tapchain.core.Chain.PackType;
import org.tapchain.core.Connector.ChainInConnector;
import org.tapchain.core.Connector.ChainOutConnector;
import org.tapchain.core.PathPack.ChainInPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack;
import org.tapchain.core.PathPack.ChainOutPathPack.Output;

public abstract class Piece implements IPiece {
	protected PartnerList partners = new PartnerList();
//	protected ArrayList<ChainInPathPack> inPack = new ArrayList<ChainInPathPack>();
	protected Map<PackType, ChainInPathPack> inPack = new EnumMap<PackType, ChainInPathPack>(PackType.class);
	protected Map<PackType, ChainOutPathPack> outPack = new EnumMap<PackType, ChainOutPathPack>(PackType.class);

	//1.Initialization
	@Override
	public ConnectionResultIO appendTo(Chain.PackType stack, IPiece target, Chain.PackType stack_target) throws ChainException {
		//if user assigns PREV FUNCTION
		if(target == null) {
			throw new ChainException(this, "appendTo()/Invalid Target/Null");
		} else if(this == target) {
			throw new ChainException(this, "appendTo()/Invalid Target/Same as Successor");
		}
		return null;
	}
	
	@Override
	public ConnectionResultO appended(Class<?> cls, Output type, Chain.PackType stack, IPiece from) throws ChainException {
		return null;
	}
	
	//2.Getters and setters
	private String name = null;
	public IPiece setName(String name) {
		this.name = name;
		return this;
	}
	public String getName() {
		if(name != null)
			return name;
		return getClass().getSimpleName();
	}

	protected ChainOutPathPack getOutPack(PackType stack) {
		return outPack.get(stack);
	}
//	protected ChainOutPathPack getOutPack(int num) {
//		if(num >= outPack.size())
//			return null;
//		return outPack.get(num);
//	}
	protected ChainInPathPack getInPack(PackType packtype) {
		return inPack.get(packtype);
	}
	
//	protected ChainInPathPack getInPack(int num) {
//		if(num >= inPack.size())
//			return null;
//		return inPack.get(num);
//	}
	public Piece setOutPackType(PackType pack, Output type) {
		getOutPack(pack).setOutType(type);
		return this;
	}
	
	public Piece setInPackType(PackType pack, ChainInPathPack.Input type) {
		getInPack(pack).setInType(type);
		return this;
	}
	public boolean hasInPath(PackType packtype) {
		return getInPack(packtype).hasPath();
	}

	public boolean hasOutPath(PackType packtype) {
		return getOutPack(packtype).hasPath();
	}

	public Collection<IPath> getLinks() {
		return partners.getPaths();
	}
	@Override
	public Collection<IPiece> getPartners() {
		return partners.getPartners();
	}
	public void setPartner(IPath o, IPiece cp) {
		partners.setPartner(o, cp);
	}
	
	@Override
	public boolean isConnectedTo(IPiece cp) {
		return partners.isConnectedTo(cp);
	}
	public boolean isConnectedTo(IPiece cp, PackType pt) {
		return partners.isConnectedTo(cp, pt);
	}
	@Override
	public PackType getPackType(IPiece cp) {
		return partners.getPackType(cp);
	}
	//3.Changing state
	
	protected ChainInPathPack addNewInPack(PackType type) {
		ChainInPathPack rtn = new ChainInPathPack(this);
		inPack.put(type, rtn);
		return rtn;
	}
	
	protected ChainOutPathPack addNewOutPack(PackType type) {
		ChainOutPathPack rtn = new ChainOutPathPack(this);
		outPack.put(type, rtn);
		rtn.setPtype(type);
		return rtn;
	}

	public void detachAll() {
		partners.detachAll();
		return;
	}
	//3-2.PathPack setting functions
	protected ChainInConnector addInPath(PackType stack) {
		return getInPack(stack).addNewConnector();
	}
	protected ChainOutConnector addOutPath(Output io, PackType stack) {
		ChainOutConnector rtn = getOutPack(stack).addNewConnector(io);
		return rtn;
	}
	public Collection<Class<?>> getInPathClasses(PackType stack) {
		return getInPack(stack).getPathClasses();
	}
	public Collection<Class<?>> getOutPathClasses(PackType stack) {
		return getOutPack(stack).getPathClasses();
	}
	public boolean addInPathClass(PackType stack, Class<?> cls) {
		return getInPack(stack).addPathClass(cls);
	}
	public boolean addOutPathClass(PackType stack, Class<?> cls) {
		return getOutPack(stack).addPathClass(cls);
	}
	//3.Input/Output functions
	public <T> T getCache(ChainInConnector i) throws InterruptedException {
		if(getInPack(PackType.PASSTHRU).contains(i)) {
			return i.<T>getCache();
		}
		else if(getInPack(PackType.EVENT).contains(i)) {
			return i.<T>getCache();
		}
		return null;
	}
	
	public boolean outputAll(PackType type, ArrayList<?> ar) throws InterruptedException {
		return getOutPack(type).outputAll(ar);
	}

	public boolean outputAllSimple(PackType type, Object obj) throws InterruptedException {
		return getOutPack(type).outputAllSimple(obj);
	}
	
	
	public boolean outputAllReset() {
//		if(aOutHeap.array.isEmpty()) return false;
		boolean rtn = false;
//		Log.e("AllReset", "Called");
//		rtn |= aOutHeap.send_reset();
//		rtn |= aOut.send_reset();
//		rtn |= aOutThis.send_reset();
//		aInEvent.send_reset();
		rtn |= getOutPack(PackType.EVENT).send_reset();
		return rtn;
	}
	protected Piece resetInPathPack(PackType pack) {
		getInPack(pack).reset();
		return this;
	}
	
	ChainPiece getNext() {
		ChainPiece tmp = null;
		if(getOutPack(PackType.PASSTHRU).isEmpty()) {
		} else {
			getOutPack(PackType.PASSTHRU).get(0);
		}
		return tmp;
	}
	
	//4.External functions related with connections(called by other thread)
	public void detached(IPiece cp) {
		partners.unsetPartner(cp);
	}
	
	
	public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
		getOutPack(PackType.PASSTHRU).waitOutput(rtn);
	}
	public void waitOutputAll(Object rtn) throws InterruptedException {
		getOutPack(PackType.PASSTHRU).waitOutputAll(rtn);
	}
	
//	boolean outputType = false;//true: wait for at least one output, false: no wait
	public boolean clearInputHeap() {
		if(getInPack(PackType.HEAP).isEmpty()) return false;
		getInPack(PackType.HEAP).reset();
		return true;
	}
	
	public boolean clearOutputHeap() {
		if(getOutPack(PackType.HEAP).isEmpty()) return false;
		getOutPack(PackType.HEAP).reset();
		return true;
		
	}
	protected boolean inputHeapAsync() {
		if(getInPack(PackType.EVENT).isEmpty()) return false;
		for(ChainInConnector a : getInPack(PackType.EVENT))
			if(a.isNotEmpty())
				return true;
//		for(ListIterator<ChainInPath> itr = aInQueueHeap.listIterator(aInQueueHeap.size()-1); itr.hasPrevious();)
//			itr.previous().pop();
		return false;
	}
	public ArrayList<Object> inputPeek(PackType type) throws InterruptedException {
		return getInPack(type).inputPeek();
	}
	public ArrayList<Object> input(PackType type) throws InterruptedException {
		return getInPack(type).input();
	}
	//4.Termination
	@Override
	public IPath detach(IPiece cp) {
		return partners.getPath((ChainPiece)cp).detach();
	}

	@Override
	public void end() {
	}

	@SuppressWarnings("serial")
	public static class PartnerList implements Serializable {
		ConcurrentHashMap<IPiece, IPath> partner;
		PartnerList() {
			partner = new ConcurrentHashMap<IPiece, IPath>();
		}
		public PackType getPackType(IPiece cp) {
			IPath _path = null;
			if((_path = partner.get(cp)) == null)
				return null;
			else
				return _path.getOutConnector().getPack().getPtype();
		}
		public ChainPiece.PartnerList setPartner(IPath o, IPiece cp) {
			partner.put(cp, o);
			return this;
		}
		public ChainPiece.PartnerList unsetPartner(IPiece cp) {
			partner.remove(cp);
			return this;
		}
//		public ChainPiece getPartner(ChainPathPair o) {
//			return partner.get(o);
//		}
		public boolean isConnectedTo(IPiece cp) {
			return partner.containsKey(cp);
		}
		public boolean isConnectedTo(IPiece cp, PackType pt) {
			IPath _path = null;
			if((_path = partner.get(cp)) != null)
				if(_path.getOutConnector().getPack().getPtype() == pt)
					return true;
			return false;
		}
		public Collection<IPath> getPaths() {
			return partner.values();
		}
		public void detachAll() {
			for(IPath pair : partner.values())
				pair.detach();
			partner.clear();
		}
		public IPath getPath(ChainPiece cp) {
			return partner.get(cp);
		}
		public Collection<IPiece> getPartners() {
			return partner.keySet();
		}
	}
}