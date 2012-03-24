package org.tapchain;

import org.tapchain.AnimationChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.Chain.ChainPiece;
import org.tapchain.Chain.ChainPiece.PackType;
import org.tapchain.PieceBlueprint.*;

import android.util.Log;


@SuppressWarnings("unchecked")
public class BlueprintManager implements Manager<PieceBlueprint> {
	AnimationChainManager parentMaker = null;
	PieceBlueprint root = null;
	PieceReservation marked = null;
	PieceReservation reserved = null;
	private Object outer;
	public BlueprintManager() {
	}
	public BlueprintManager SetParent(AnimationChainManager _parent) {
		parentMaker = _parent;
		return this;
	}
	public AnimationChainManager GetParent() {
		return parentMaker;
	}
	public AnimationChainManager Exit() {
		return GetParent();
	}
	public BlueprintManager AddHandler(HeapFilter ev, MoveViewEffect ef) {
		return this;
	}
	public BlueprintManager Return(PieceReservation bp) {
		if(bp == null) {
			Error(new ChainException(this, "returnToMark: no mark to return"));
		}
		reserved = bp;
		return this;
	}
	public BlueprintManager Error(ChainException e) {
		Log.e("Error ACM", e.err);
		return this;
	}
	public BlueprintManager New(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return New(new PieceBlueprint(_cls, args));
	}
	public BlueprintManager NewLocal(Class<? extends BasicPiece> _cls, Object parent, BasicPiece... args) {
		return New(new PieceBlueprint(_cls, args).setLocalClass(parent.getClass(), parent));
	}
	public BlueprintManager New(PieceBlueprint pbp) {
		root = pbp;
		marked = root.This;
		reserved = root.This;
		return this;
	}
	public BlueprintManager add(PieceBlueprint _pbp, ChainPiece... args) {
		reserved = root.addLocal(_pbp, args);
		return this;
	}
	public BlueprintManager add(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return add(new PieceBlueprint(_cls), args);
	}
	public PieceReservation getLast() {
		return reserved;
	}
	@Override
	public BlueprintManager reset(PieceBlueprint _pbp, ChainPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.FAMILY, reserved, PackType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager args(PieceBlueprint _pbp, ChainPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.HEAP, reserved, PackType.HEAP);
		return this;
	}
	@Override
	public BlueprintManager and(PieceBlueprint _pbp, ChainPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		reserved.Append(PackType.PASSTHRU, past, PackType.PASSTHRU);
		return this;
	}
	public BlueprintManager parent(PieceBlueprint _pbp, BasicPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.HEAP, reserved, PackType.FAMILY);
		return this;
	}
	public BlueprintManager child(PieceBlueprint _pbp, BasicPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		reserved.Append(PackType.PASSTHRU, past, PackType.FAMILY);
		return this;
	}
	
	public BlueprintManager args(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return args(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager reset(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return reset(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager and(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return and(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager parent(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return parent(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager child(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return child(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager because(Class<? extends BasicPiece> _cls, BasicPiece... args) {
		return because(new PieceBlueprint(_cls), args);
	}
	public BlueprintManager args(BasicPiece bp, BasicPiece... args) {
		return args(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager reset(BasicPiece bp, BasicPiece... args) {
		return reset(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager and(BasicPiece bp, BasicPiece... args) {
		return and(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager parent(BasicPiece bp, BasicPiece... args) {
		return parent(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager child(BasicPiece bp, BasicPiece... args) {
		return child(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager because(BasicPiece bp, BasicPiece... args) {
		return because(new PieceBlueprintStatic(bp), args);
	}
	public BlueprintManager because(PieceBlueprint _pbp, BasicPiece... args) {
		PieceReservation past = reserved;
		add(_pbp, args);
		past.Append(PackType.EVENT, reserved, PackType.EVENT);
		return this;
	}
	@Override
	public BlueprintManager Save() {
		parentMaker.GetFactory().Register(root);
		return this;
	}
	public BlueprintManager _mark() {
		marked = reserved;
		return this;
	}
	public BlueprintManager _return() {
		reserved = marked;
		return this;
	}
	public BlueprintManager setview(Class<? extends BasicView> _cls, BasicPiece... args) {
		if(_cls.isMemberClass())
			root.setview(new PieceBlueprint(_cls, args).setLocalClass(outer.getClass(), outer));
		else
			root.setview(new PieceBlueprint(_cls, args));
		return this;
	}
//	public BlueprintManager setviewLocal(Class<? extends BasicView> _cls, BasicPiece... args) {
//		if(_cls.isMemberClass())
//			root.setview(new PieceBlueprint(_cls, args).setLocalClass(outer.getClass(), outer));
//		return this;
//	}
	public BlueprintManager setOuterInstanceForInner(Object outer) {
		this.outer = outer;
		return this;
	}
}
