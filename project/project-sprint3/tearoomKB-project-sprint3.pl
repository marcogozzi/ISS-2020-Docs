%===========================================
% tearoomkb.pl
%===========================================

maxStayTime(10000).
debug(waiter,false). 
debug(robot,false). 

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
%% Configuration
%% ------------------------------------------ 

stepTime(400).
stepFailWaitTime(5000).
%%debug(x).
%%debugwaiter(x).

%% ------------------------------------------ 
%% Positions
%% ------------------------------------------ 
%%pos( home, 0, 0 ).
%%pos( entrancedoor, 4, 1 ).
%%pos( exitdoor, 4, 6 ).
%%pos( barman, 0, 5 ).
%%pos( teatable(1), 2, 3 ).
%%pos( teatable(2), 2, 5 ).
pos( home, 0, 0 ).
pos( entrancedoor, 1, 4 ).
pos( exitdoor, 5, 4 ).
pos( barman, 5, 0 ).
pos( teatable(1), 2, 2 ).
pos( teatable(2), 4, 2 ).
 
%% ------------------------------------------ 
%% Teatables
%% teatable(Num, {free, reserved(Cid), busy(Cid)}, {clean, dirty, cleanedA, cleanedB}).
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
	
dirtyTable(Num) :- teatable(Num, free, dirty).	
	
	
cleanTable(N)	 :-
	retract( teatable( N, free, dirty ) ),
	!,
	asserta( teatable( N, free, clean ) ).

cleanTable(N,NewState)	 :-
	retract( teatable( N, free, dirty ) ),
	!,
	asserta( teatable( N, free, cleanedA ) ),
	teatable(N,free,NewState).
	
cleanTable(N,NewState)	 :-
	retract( teatable( N, free, cleanedA ) ),
	!,
	asserta( teatable( N, free, cleanedB ) ),
	teatable(N, free, NewState).
	
cleanTable(N,NewState)	 :-
	retract( teatable( N, free, cleanedB ) ),
	!,
	asserta( teatable( N, free, clean ) ),
	teatable(N, free, NewState).
 
 
stateOfTeatable( [teatable1(V1,V2)] ) :-
	teatable( 1, V1, V2 ).
	
%%stateOfTeatables( V ) :- findall(teatable(N,F,C), teatable(N,F,C), V).
stateOfTeatables( [teatable(1, V1, V2),teatable(2, V3, V4)] ) :-
	teatable( 1, V1, V2 ),
	teatable( 2, V3, V4 ).

%% ------------------------------------------ 
%% Waiter( {State(Cid), State(TableNum)} )

%% Cid
%% checkAvailability, reachfor, reached, deployed
%% goTakeOrder, takingOrder, getOrderReady, atbar
%% serving, getPayment, paymentOf, escorted 

%% TableNum
%% gocleanteatable, atteatable, cleaned, cleaning

%% Other
%% gorest(0), rest(0)
%% ------------------------------------------ 

waiter( rest(0) ).	

%%update textual state, keep map location
updateWaiterState(Old,New) :- 
	retract(waiter(Old)),
	!,
	assert(waiter(New)).
	
%%get both waiter and destination coordinates from destination name
coordinatesTo(PositionName, X, Y) :-
	pos(PositionName, X, Y).
	
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
	 
	
