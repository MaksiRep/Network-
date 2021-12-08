#ifndef MULTICASTGROUP_H
#define MULTICASTGROUP_H

#include <ctime>
#include <map>
#include <iostream>

class manageData{

private:


    std::map<int, time_t> infoList;


public:


    void checking(int id){
        infoList[id] = time(nullptr);
    }


    void print(){
        std::cout << "Count active " << infoList.size() << " : " << std::endl;
        for (const auto& copy: infoList){
            char buf[128];
            const auto infoTime = localtime(&copy.second);
            strftime(buf, sizeof(buf), "%H:%M:%S", infoTime);
            std::cout << "ID: " << copy.first << "  last  " << buf << std::endl;
        }
    }


    void disposal(){
        std::map<int, time_t>::iterator temp;
        for (temp = infoList.begin(); temp != infoList.end() && temp->second != 0; ++temp){
            if (time(nullptr) - temp->second > 10){
                std::cout << "DISPOSAL: " << temp->first << std::endl;
                infoList.erase(temp);
                print();
            }
        }
    }


};


#endif

