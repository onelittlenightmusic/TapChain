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
import org.tapchain.core.Connector.InConnector;
import org.tapchain.core.Connector.OutConnector;
import org.tapchain.core.PathPack.InPathPack;
import org.tapchain.core.PathPack.OutPathPack;
import org.tapchain.core.PathPack.OutPathPack.Output;

public abstract class Piece<PARTNER extends IPiece> implements IPiece<PARTNER> {
	protected PartnerList partnerList = new PartnerList();
	protected Map<PathType, InPathPack> inPack = new EnumMap<PathType, InPathPack>(PathType.class);
	protected Map<PathType, OutPathPack> outPack = new EnumMap<PathType, OutPathPack>(PathType.class);

	//1.Initialization
	@Override
	public ConnectionResultIO appendTo(PathType stack, IPiece target, PathType stack_target) throws ChainException {
		//if user assigns PREV FUNCTION
		if(target == null) {
			throw new ChainException(this, "appendTo()/Invalid Target/Null");
		} else if(this == target) {
			throw new ChainException(this, "appendTo()/Invalid Target/Same as Successor");
		}
		return null;
	}

	@Override
	public ConnectionResultO appended(PathType stack, IPiece from, Output type) throws ChainException {
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
		return getClass().getSimpleName()+getId();
	}

	@Override
	public OutPathPack getOutPack(PathType stack) {
		return outPack.get(stack);
	}
	@Override
	public InPathPack getInPack(PathType packtype) {
		return inPack.get(packtype);
	}

	public Piece setOutPackType(PathType pack, Output type) {
		getOutPack(pack).setOutType(type);
		return this;
	}

	public Piece setInPackType(PathType pack, InPathPack.Input type) {
		getInPack(pack).setInType(type);
		return this;
	}
	public boolean hasInPath(PathType pathType) {
		return getInPack(pathType).hasPath();
	}

	public boolean hasOutPath(PathType pathType) {
		return getOutPack(pathType).hasPath();
	}

	public Collection<IPath> getLinks() {
		return partnerList.getPaths();
	}

	@Override
	public Collection<PARTNER> getPartners() {
		return partnerList.getPartners();
	}

	@Override
	public Collection<PARTNER> getPartners(PathType pathType, boolean out) {
		return partnerList.getPartners(pathType, out, this);
	}
	public void setPartner(IPath o, IPiece cp) {
		partnerList.setPartner(o, cp);
	}

	@Override
	public boolean isConnectedTo(IPiece cp) {
		return partnerList.isConnectedTo(cp);
	}

	@Override
	public boolean isConnectedTo(IPiece cp, PathType pt) {
		return partnerList.isConnectedTo(cp, pt);
	}

	@Override
	public boolean isConnectedTo(PathType pathType, boolean out) {
		if(out)
			return hasOutPath(pathType);
		else
			return hasInPath(pathType);
	}
	@Override
	public PathType getPackType(IPiece cp) {
		return partnerList.getPackType(cp);
	}
	//3.Changing state

	protected InPathPack addNewInPack(PathType type) {
		InPathPack rtn = new InPathPack(this);
		inPack.put(type, rtn);
		rtn.setPtype(type);
		return rtn;
	}

	protected OutPathPack addNewOutPack(PathType type) {
		OutPathPack rtn = new OutPathPack(this);
		outPack.put(type, rtn);
		rtn.setPtype(type);
		return rtn;
	}

	public void detachAll() {
		partnerList.detachAll();
		return;
	}
	//3-2.PathPack setting functions
	protected InConnector addInPath(PathType stack) {
		return getInPack(stack).addNewConnector();
	}
	protected OutConnector addOutPath(Output io, PathType stack) {
		OutConnector rtn = getOutPack(stack).addNewConnector(io);
		return rtn;
	}
	public Collection<ClassEnvelope> getInPathClasses(PathType stack) {
		return getInPack(stack).getPathClasses();
	}
	public Collection<ClassEnvelope> getOutPathClasses(PathType stack) {
		return getOutPack(stack).getPathClasses();
	}
	public boolean setInPathClass(PathType stack, ClassEnvelope clz) {
		return getInPack(stack).addPathClass(clz);
	}
	public boolean setOutPathClass(PathType stack, ClassEnvelope clz) {
		return getOutPack(stack).addPathClass(clz);
	}
	//3.Input/Output functions
	public <T> T getCache(InConnector i) throws InterruptedException {
		if(getInPack(PathType.PASSTHRU).contains(i)) {
			return i.<T>getCache();
		}
		else if(getInPack(PathType.EVENT).contains(i)) {
			return i.<T>getCache();
		}
		return null;
	}

	public boolean outputAll(PathType type, ArrayList<?> ar) throws InterruptedException {
		return getOutPack(type).outputAll(ar);
	}

	public boolean outputAllSimple(PathType type, Object obj) throws InterruptedException {
		return getOutPack(type).outputAllSimple(obj);
	}


	public boolean outputAllReset() {
		boolean rtn = false;
		rtn |= getOutPack(PathType.EVENT).send_reset();
		return rtn;
	}
	protected Piece resetInPathPack(PathType pack) {
		getInPack(pack).reset();
		return this;
	}

	//4.External functions related with connections(called by other thread)
	public void detached(IPiece cp) {
		partnerList.unsetPartner(cp);
	}


	public void waitOutput(ArrayList<Object> rtn) throws InterruptedException {
		getOutPack(PathType.PASSTHRU).waitOutput(rtn);
	}
	public void waitOutputAll(Object rtn) throws InterruptedException {
		getOutPack(PathType.PASSTHRU).waitOutputAll(rtn);
	}

	public boolean clearPull() {
		if(getInPack(PathType.OFFER).isEmpty()) return false;
		getInPack(PathType.OFFER).reset();
		return true;
	}

	public boolean clearPush() {
		getOutPack(PathType.OFFER).reset();
		return true;

	}
	protected boolean inputHeapAsync() {
		if(getInPack(PathType.EVENT).isEmpty()) return false;
		for(InConnector a : getInPack(PathType.EVENT))
			if(a.isNotEmpty())
				return true;
		return false;
	}
	public ArrayList<Object> inputPeek(PathType type) throws InterruptedException, IAxon.AxonException {
		return getInPack(type).inputPeek();
	}
	public ArrayList<Object> input(PathType type) throws InterruptedException {
		return getInPack(type).input();
	}
	//4.Termination
	@Override
	public IPath detach(IPiece p) {
		IPath partner = partnerList.getPath((ChainPiece)p);
		if(partner == null)
			return null;
		return partner.detach();
	}

	@Override
	public IPiece end() {
		return this;
	}

	@SuppressWarnings("serial")
	public static class PartnerList<PARTNER extends IPiece> implements Serializable {
		ConcurrentHashMap<PARTNER, IPath> partner;
		PartnerList() {
			partner = new ConcurrentHashMap<PARTNER, IPath>();
		}
		public PathType getPackType(IPiece cp) {
			IPath _path = null;
			if((_path = partner.get(cp)) == null)
				return null;
			else
				return _path.getOutConnector().getPack().getPathType();
		}
		public ChainPiece.PartnerList setPartner(IPath o, PARTNER cp) {
			partner.put(cp, o);
			return this;
		}
		public ChainPiece.PartnerList unsetPartner(IPiece cp) {
			partner.remove(cp);
			return this;
		}
		public boolean isConnectedTo(IPiece cp) {
			return partner.containsKey(cp);
		}
		public boolean isConnectedTo(IPiece cp, PathType pt) {
			IPath _path = null;
			if((_path = partner.get(cp)) != null)
				if(_path.getPathType() == pt)
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
		public Collection<PARTNER> getPartners() {
			return partner.keySet();
		}

		public Collection<PARTNER> getPartners(PathType pathType, boolean out, Piece piece) {
			Collection<PARTNER> rtn = new ArrayList<PARTNER>();
			if(getPartners().isEmpty())
				return rtn;
			for(Map.Entry<PARTNER, IPath> entry: partner.entrySet()) {
				if (entry.getValue().getPathType() == pathType)
					if(entry.getValue().getOut(piece) == out)
						rtn.add(entry.getKey());
			}
			return rtn;
		}
	}
}
