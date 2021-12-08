#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <sys/types.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <cstring>
#include <arpa/inet.h>
#include <map>
#include <poll.h>
#include "manageData.h"

sa_family_t AF_INET_TYPE;
struct sockaddr_in multicastGroupIPV4{};
struct sockaddr_in6 multicastGroupIPV6{};
struct sockaddr_in sockaddrIn{};
struct sockaddr_in6 addressV6{};
struct ip_mreq membershipRequest{};
struct ipv6_mreq membershipRequestV6{};
struct pollfd recvSocketPoll{};
bool FLAG_IP_VERSION_6 = false;
int sentSocket, receivSocket;
char buf[1024];
manageData group{};
int id = getpid();
const std::string msg = "CPYDTCTR-" + std::to_string(id); // используем pid как идентификатор
u_int FLAG_YES = 1;
u_int FLAG_NO = 0;

void createReqAndJoinMultGroup (char **argv) {
    if (FLAG_IP_VERSION_6){
        membershipRequestV6.ipv6mr_interface = 0;
        inet_pton(AF_INET6, argv[1], &membershipRequestV6.ipv6mr_multiaddr);
        if (setsockopt(receivSocket, IPPROTO_IPV6, IPV6_JOIN_GROUP, &membershipRequestV6, sizeof(membershipRequestV6)) < 0){
            perror ("FAIL: SETSOCKOPT");
            exit(-1);
        }
    }else{
        membershipRequest.imr_multiaddr.s_addr = inet_addr(argv[1]);
        membershipRequest.imr_interface.s_addr = htonl(INADDR_ANY);
        if (setsockopt(receivSocket, IPPROTO_IP, IP_ADD_MEMBERSHIP, &membershipRequest, sizeof(membershipRequest)) < 0){
            perror ("FAIL: SETSOCKOPT");
            exit(-1);
        }
    }
}


void bindSocket () {
    if (FLAG_IP_VERSION_6) {
        if (bind(receivSocket, (struct sockaddr *) &addressV6, sizeof(addressV6)) < 0) {
            perror("FAIL: BIND SOCKET");
            exit(-1);
        }
    }else {
        if (bind(receivSocket, (struct sockaddr *) &sockaddrIn, sizeof(sockaddrIn)) < 0) {
            perror("FAIL: BIND SOCKET");
            exit(-1);
        }
    }
}


void initAddress (char **argv) {
    if (FLAG_IP_VERSION_6) {
        addressV6.sin6_addr = in6addr_any;
        addressV6.sin6_port = htons(strtol(argv[2], nullptr, 0));
        addressV6.sin6_family = AF_INET6;
    }else {
        sockaddrIn.sin_addr.s_addr = htonl(INADDR_ANY);
        sockaddrIn.sin_port = htons(strtol(argv[2], nullptr, 0));
        sockaddrIn.sin_family = AF_INET;
    }
}


void reuseAddr () {
    if (setsockopt(receivSocket, SOL_SOCKET, SO_REUSEADDR, &FLAG_YES, sizeof(FLAG_YES)) < 0){
        perror("FAIL: REUSE OPTION");
        exit(-1);
    }
}


void createSockets () {
    if ((receivSocket = socket(AF_INET_TYPE, SOCK_DGRAM, 0)) < 0 || (sentSocket = socket(AF_INET_TYPE, SOCK_DGRAM, 0)) < 0){
        perror("FAIL: CREATE SOCKETS");
        exit(-1);
    }
}


void initMulticastGroup (char **argv) {
    if (FLAG_IP_VERSION_6) {
        memset(&multicastGroupIPV6, 0, sizeof(multicastGroupIPV6));
        multicastGroupIPV6.sin6_family = AF_INET_TYPE;
        multicastGroupIPV6.sin6_port = htons(strtol(argv[2], nullptr, 0));
        inet_pton(AF_INET6, argv[1], &multicastGroupIPV6.sin6_addr);
    }else {
        memset(&multicastGroupIPV4, 0, sizeof(multicastGroupIPV4));
        multicastGroupIPV4.sin_family = AF_INET_TYPE;
        multicastGroupIPV4.sin_port = htons(strtol(argv[2], nullptr, 0));
        multicastGroupIPV4.sin_addr.s_addr = inet_addr(argv[1]);
    }
}


sa_family_t versionIP (char **argv) {
    if (strchr(argv[1], ':') != nullptr){
        FLAG_IP_VERSION_6 = true;
        return AF_INET6;
    }else{
        return AF_INET;
    }
}


size_t messageSend (){
    if (FLAG_IP_VERSION_6){
        return sendto(sentSocket, (&msg)->c_str(), (&msg)->length(), FLAG_NO, reinterpret_cast<sockaddr * >(&multicastGroupIPV6), sizeof(multicastGroupIPV6));
    }
    else{
        return sendto(sentSocket, (&msg)->c_str(), (&msg)->length(), FLAG_NO, reinterpret_cast<sockaddr * >(&multicastGroupIPV4), sizeof(multicastGroupIPV4));
    }
}


void correctInputData (int argc, char* argv[]) {
    if (argc != 3){
        std::cerr << "----------------------------Invalid request----------------------------" << std::endl;
        std::cerr << "Correct request: " << argv[0] << " IP(226.0.0.0) Port(8080)" << std::endl;
        exit(-1);
    }
}

void run () {
    while (true){
        int count = poll(&recvSocketPoll, 1, 2000);
        if (count < 0){
            std::cerr << "FAIL: POLL" << std::endl;
            exit(-1);
        }else if (count == 0) {
            messageSend();
        }else {
            ssize_t bytes = recv(receivSocket, buf, 1024, 0);
            if (bytes < 0){
                std::cerr << "FAIL: READ" << std::endl;
                exit(-1);
            }else if (bytes > 0){
                if (((std::string)buf).substr(0, 9) == "CPYDTCTR-") {
                    int newId = std::stoi(((std::string)buf).substr(9, bytes));
                    if (newId != id){
                        group.checking(newId); // обновление информации
                    }
                }
                group.print();
                group.disposal();
            }
        }
    }
}


int main(int argc, char* argv[]){

    correctInputData(argc, argv);      //проверка введенных данных

    AF_INET_TYPE = versionIP(argv);    //переменная IP версии

    initMulticastGroup(argv);          //инициализация manageData

    createSockets();                   //создание сокетов

    reuseAddr();                       //разрешение повторного использования адреса

    initAddress(argv);                 //инициализация полей

    bindSocket();                      //bind сокета

    createReqAndJoinMultGroup(argv);   //создание request и join multicast group

    recvSocketPoll.fd = receivSocket;  //инициализация полей
    recvSocketPoll.events = POLLIN;
    recvSocketPoll.revents = 0;

    messageSend();                     //пересылки

    run();                             //запуск

}
