%===========================================
% tearoomkb.pl
%===========================================
 
 debug(false).
 
%% ------------------------------------------ 
%% Clients
%% client(Cid, State).
%% State in {accepted, onhold, entering, choosing, placeorder, orderplaced, consuming, paying, leaving, left}
%% ------------------------------------------ 

updateClient(Cid,Msg) :- 
	retract(client(Cid,_)),
	!,
	assert(client(Cid,Msg)).


clients(L) :- findall(client(Cid,S),client(Cid,S),L).

%% ------------------------------------------ 
%% Teatables
%% teatable(Num, {free, reserved(Cid), busy(Cid)}, {clean, dirty}).
%% ------------------------------------------ 
teatable( 1, free, clean ).
teatable( 2, free, clean ). 

tableavailable(N):- teatable(N,	free, clean ).

numavailabletables(N) :-
	findall( N, teatable( N, free, clean ), NList),
	length( NList, N ).

reserveTable(N,Cid)	 :-
	retract( teatable( N, free, clean ) ) ,
	!,
	assert( teatable( N, reserved(Cid), clean ) ),
	assert( client(Cid, accepted) ).
	
%%reserveTable(_,_).	

%%reservedTable(N, Cid, Clean) :- teatable( N, reserved(Cid), Clean ). %%tavolo prenotato da Cid

engageTable(N,Cid)	 :-
	retract( teatable( N, reserved(Cid), clean ) ) ,
	!,
	assert( teatable( N, busy(Cid), dirty ) ),
	updateClient( client(Cid, choosing) ).

freeTable(N, Cid) :-
	retract( teatable( N, busy(Cid), dirty ) ),
	!,
	assert( teatable( N, free, dirty ) ).
freeTable(_,_).	
	
dirtyTable(Num) :- teatable(Num, free, dirty).	
	
cleanTable(N)	 :-
	%% stdout <- println( tearoomkb_cleanTable(N) ),
	retract( teatable( N, free, dirty ) ) ,
	!,
	assert( teatable( N, free, clean ) ).
cleanTable(N).	
 
stateOfTeatable( [teatable1(V1,V2)] ) :-
	teatable( 1, V1, V2 ).
	
stateOfTeatables( [teatable(1, V1, V2),teatable(2, V3, V4)] ) :-
	teatable( 1, V1, V2 ),
	teatable( 2, V3, V4 ).

%% ------------------------------------------ 
%% Waiter
%% ------------------------------------------ 

waiter( athome ).	

updateWaiter(Old,New) :- 
	retract(waiter(Old)),
	!,
	assert(waiter(New)).

%% ------------------------------------------ 
%% Hall
%% ------------------------------------------ 
hall(free).

freeHall(X) :- 
retract(hall(free)),
!,
assert(hall(busy)).

occupyHall(X) :- 
retract(hall(busy)),
!,
assert(hall(free)).


%% ------------------------------------------ 
%% Room as a whole
%% ------------------------------------------ 
roomstate(  state( waiter(S), tables(V), clients(C) )  ):-
	 waiter(S), stateOfTeatables(V), clients(C) .
	 
	
