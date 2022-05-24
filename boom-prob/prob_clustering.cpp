#include <iostream>
#include<cmath>
#include <time.h>
using namespace std;

#define TAKE_BIT(x, pos) (((x) >> (pos)) & 0x1)

int sbox_4[16] = {12,6,9,0,1,10,2,11,3,8,5,13,4,14,7,15};


int TWEAKEY_P[16] = {9,15,8,13,10,14,12,11,0,1,2,3,4,5,6,7};
int TWEAKEY_P_inv[16] = {8,9,10,11,12,13,14,15,2,0,4,7,6,3,5,1};
int DDT4[16][16]={0};
int TK2[32]={1,2,4,9,3,6,13,10,5,11,7,15,14,12,8,1,2,4,9,3,6,13,10,5,11,7,15,14,12,8};
int TK3[32]={1,8,12,14,15,7,11,5,10,13,6,3,9,4,2,1,8,12,14,15,7,11,5,10,13,6,3,9,4,2};
int TK4v1[30]={1,2,5,9,3,7,12,10,4,11,6,14,15,13,8,1,2,5,9,3,7,12,10,4,11,6,14,15,13,8};
int TK4v2[30]={1,4,3,13,5,7,14,8,2,9,6,10,11,15,12,1,4,3,13,5,7,14,8,2,9,6,10,11,15,12};

/* DDT for the 4-bit Sbox */
int differential_distribution_table4(int DDT[16][16]){
    int din,x;

    for (din=0; din<16; din++){
        for (x=0;x<16;x++){
            DDT[din][sbox_4[x]^sbox_4[x^din]]++;
        }
    }
    return 0;
}



void skinny_64_256_p_v2()
{
    long double prob=0;
    long double clprob=0;
    
    for(int k11=1; k11<16; k11++) {
        if(DDT4[13][k11]>0) {
            for(int k12=1; k12<16; k12++) {
                if(DDT4[13][k12]>0 ) {
                    prob = DDT4[13][k11]*DDT4[13][k12]*DDT4[k11^0x6][4]*DDT4[k12][4];
                    prob *= DDT4[13][2]*DDT4[4][2]*DDT4[14][9];
                    prob *= DDT4[1][9]*DDT4[1][8];
                    clprob += prob*prob;
                }
            }
        }
    }
    cout << "The probability of r0=12 round for skinny_64_256 v2:"<<endl;
    cout << "p="<<log(sqrt(clprob))/log(2.0)-36<< endl;
}



void skinny_64_256_q_v1()
{
    long double prob=0;
    long double clprob=0;
    
    
    for(int k12=1; k12<16; k12++) {
        if(DDT4[k12][10]>0 ) {
            for(int k13=1; k13<16; k13++) {
                if(DDT4[k13][k12]>0 ) {
                     prob = DDT4[k12][10]*DDT4[k13][k12];
                     prob *= DDT4[5][2]*DDT4[5][k13]*DDT4[5][k13]*DDT4[5][k13];
                     clprob += prob*prob;
                }
            }
        }
    }

    cout << "The probability of r1=13 round for skinny_64_256 v1:"<<endl;
    cout << "q="<<log(sqrt(clprob))/log(2.0)-24<< endl;
}


void Key(unsigned char keyCells[4][4][4],int version)
{
    int i, j, k;
    unsigned char pos;
    unsigned char keyCells_tmp[4][4][4],tmp;

    // update the subtweakey states with the permutation
    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){
                //application of the TWEAKEY permutation
                pos=TWEAKEY_P[j+4*i];
                keyCells_tmp[k][i][j]=keyCells[k][pos>>2][pos&0x3];
            }
        }
    }

    // update the subtweakey states with the LFSRs
    for(k = 0; k <4; k++){
        for(i = 0; i <= 1; i++){
            for(j = 0; j < 4; j++){
                //application of LFSRs for TK updates
                if (k==1)
                {
                    keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<1)&0xE)^((keyCells_tmp[k][i][j]>>3)&0x1)^((keyCells_tmp[k][i][j]>>2)&0x1);
                }
                else if (k==2)
                {
                    keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>1)&0x7)^((keyCells_tmp[k][i][j])&0x8)^((keyCells_tmp[k][i][j]<<3)&0x8);
                }
                else if (k==3)
                {
                    //v1
                    if (version ==1) {
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<1)&0xC)|(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],2))<<1)&0x2)|(TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],2)^TAKE_BIT(keyCells_tmp[k][i][j],3));
                    }
                    else {
                    //v2
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<2)&0xC)|(((TAKE_BIT(keyCells_tmp[k][i][j],3)^TAKE_BIT(keyCells_tmp[k][i][j],2))<<1)&0x2)|(TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],2));
                    }
                }
            }
        }
    }

    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){
                keyCells[k][i][j]=keyCells_tmp[k][i][j];
            }
        }
    }
    
    // compute the subkey
    for(i = 0; i <= 1; i++)
    {
        for(j = 0; j < 4; j++)
        {
            tmp = keyCells[0][i][j];
            tmp ^= keyCells[1][i][j] ^ keyCells[2][i][j] ^ keyCells[3][i][j];
            printf("%d,", tmp);
        }
    }
    printf("\n");
    // print the key state
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[0][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[1][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[2][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[3][i][j]);
    printf("\n");
    printf("\n");
}

void Key_inv(unsigned char keyCells[4][4][4],int version)
{
    int i, j, k;
    unsigned char pos;
    unsigned char keyCells_tmp[4][4][4], tmp;

    // update the subtweakey states with the permutation
    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){
                //application of the inverse TWEAKEY permutation
                pos=TWEAKEY_P_inv[j+4*i];
                keyCells_tmp[k][i][j]=keyCells[k][pos>>2][pos&0x3];
            }
        }
    }

    
    // update the subtweakey states with the LFSRs
    for(k = 0; k <4; k++){
        for(i = 2; i <= 3; i++){
            for(j = 0; j < 4; j++){
                //application of inverse LFSRs for TK updates
                if (k==1)
                {
                    keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>1)&0x7)^((keyCells_tmp[k][i][j]<<3)&0x8)^((keyCells_tmp[k][i][j])&0x8);
                }
                else if (k==2)
                {
                    keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<1)&0xE)^((keyCells_tmp[k][i][j]>>3)&0x1)^((keyCells_tmp[k][i][j]>>2)&0x1);
                }
                else if (k==3)
                {
                    //v1 inv
                    if (version ==1) {
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>1)&0x7)^(TAKE_BIT(keyCells_tmp[k][i][j],3)&0x1)^(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],2)^TAKE_BIT(keyCells_tmp[k][i][j],3))<<3)&0x8);
                    }
                    else {
                    //v2 inv
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>2)&0x3)^(((TAKE_BIT(keyCells_tmp[k][i][j],3)^TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],0))<<3)&0x8)^(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],3))<<2)&0x4);
                    }
                }
            }
        }
    }

    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){
                keyCells[k][i][j]=keyCells_tmp[k][i][j];
            }
        }
    }

    // compute the subkey
    for(i = 0; i <= 1; i++)
    {
        for(j = 0; j < 4; j++)
        {
            tmp = keyCells[0][i][j];
            tmp ^= keyCells[1][i][j] ^ keyCells[2][i][j] ^ keyCells[3][i][j];
            printf("%d,", tmp);
        }
    }
    printf("\n");
    
    // print the key state
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[0][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[1][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[2][i][j]);
    printf("\n");
    for(i = 0; i < 4; i++) for(j = 0; j < 4; j++) printf("%x,", keyCells[3][i][j]);
    printf("\n");
    
    printf("\n");
}

void mostzero0()
{
    int DTK[30];
    
    for (int i=0; i<16; i++)
        for (int j=0; j<16; j++)
            for (int k=0; k<16; k++)
                for (int h=0; h<16; h++) {
                    int length = 0;
                    for (int times=0; times<15; times++) {
                        DTK[times]= i;
                        if (j != 0)
                            DTK[times] = DTK[times]^TK2[j+times];
                        if (k != 0)
                            DTK[times] = DTK[times]^TK3[k+times];
                        if (h != 0)
                            DTK[times] = DTK[times]^TK4v1[h+times];
                        if (DTK[times]==0)
                            length ++;
                    }
                    if (length > 4) {
                        cout << length <<": "<< i << ' ' ;
                        if (j!=0)
                            cout << TK2[j] << ' ' ;
                        else
                            cout << j << ' ' ;
                        if (k!=0)
                            cout << TK3[k] << ' ' ;
                        else
                            cout << k << ' ' ;
                        if (h!=0)
                            cout << TK4v1[h] << ": ";
                        else
                            cout << h << ": ";
                        int startnum = -1;
                        int startindex = 0;
                        int index[4];
                        for (int times=0; times<15; times++) {
                            cout << DTK[times] << ' ';
                            if (DTK[times]==0) {
                                if (startnum == -1)
                                    startnum = times;
                                if(startindex < 4)
                                    index[startindex] = times-startnum;
                                startindex ++;
                            }
                        }
                        cout << endl;
                    }
    }
}
void generatekey()
{
    // v1
    unsigned char MK_v1upper[4][4][4] = {
     //TK0
    0, 0, 5, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK1
    0, 0, 6, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK2
    0, 0, 10, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK3
    0, 0, 2, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0};
    unsigned char MK_v1lower[4][4][4] = {
    //TK0
    0, 0, 0, 0,
    0, 4, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK1
    0, 0, 0, 0,
    0, 14, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK2
    0, 0, 0, 0,
    0, 2, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    //TK3
    0, 0, 0, 0,
    0, 11, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0};
    

    // v2
    unsigned char MK_v2upper[4][4][4] = {
     //TK0
    0, 6, 0, 2,
    0, 0, 0, 0,
    0, 0, 0, 13,
    0, 0, 0, 0,
    //TK1
    0, 9, 0, 8,
    0, 0, 0, 0,
    0, 0, 0, 3,
    0, 0, 0, 0,
    //TK2
    0, 12, 0, 11,
    0, 0, 0, 0,
    0, 0, 0, 8,
    0, 0, 0, 0,
    //TK3
    0, 10, 0, 9,
    0, 0, 0, 0,
    0, 0, 0, 5,
    0, 0, 0, 0};
    unsigned char MK_v2lower[4][4][4] = {
    //TK0
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 2,
    //TK1
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 6,
    //TK2
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 9,
    //TK3
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 1};
    
    for(int i=0; i<40; i++)
    {
        cout << i+1 << endl;
        //Key_inv(MK_v2upper,2);
        //Key(MK_v2lower,2);
        Key(MK_v1upper,1);
    }

}

int main()
{
    differential_distribution_table4(DDT4);
    skinny_64_256_q_v1();
    skinny_64_256_p_v2();
    //mostzero0();
    //generatekey();
    return 0;
}
