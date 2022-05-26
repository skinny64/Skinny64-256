#include"cuda_runtime.h"
#include"device_launch_parameters.h"
#include"curand_kernel.h"// this lib shoulb be included
#include<ctime>
#include<iostream>
#include<random>

using namespace std;
#define TAKE_BIT(x, pos) (((x) >> (pos)) & 0x1)


//v2 26r
/*__constant__ int version = 2;
__constant__ int N2=1024;
int hN2=1024;

int hN1=512;
__constant__ int N1=512;
__constant__ int r=6;
__constant__ unsigned char dk1[32] = {00,0x06, 00,0x20, 00,00, 00,0xd0, 
                                      00,0x0b, 00,0x60, 00,00, 00,0x70, 
                                      00,0x0a, 00,0x90, 00,00, 00,0x50, 
                                      00,0x03, 00,0x10, 00,00, 00,0x60};
__constant__ unsigned char dk2[32] = {00,00, 00,00, 00,00, 0x02,00, 
                                      00,00, 00,00, 00,00, 0x0f,00, 
                                      00,00, 00,00, 00,00, 0x0e,00, 
                                      00,00, 00,00, 00,00, 0x0e,00};
__constant__ unsigned char dp[8] = {00,0x09, 00,00, 00,00, 00,0x09}; 
__constant__ unsigned char dc[8] = {00,00, 00,00, 00,00, 00,00};*/


//v1 30r
__constant__ int version = 1;
__constant__ int N2=1024*8;
int hN2=1024*8;

int hN1=512*8;
__constant__ int N1=512*8;

__constant__ int r=5;
__constant__ unsigned char dk1[32] = {00,00, 00,00, 00,00, 00,0x01, 
                                      00,00, 00,00, 00,00, 00,0x08, 
                                      00,00, 00,00, 00,00, 00,00, 
                                      00,00, 00,00, 00,00, 00,0x08};
__constant__ unsigned char dk2[32] = {00,00, 00,00, 00,00, 0x01,00, 
                                      00,00, 00,00, 00,00, 0x06,00, 
                                      00,00, 00,00, 00,00, 00,00, 
                                      00,00, 00,00, 00,00, 0x07,00};
__constant__ unsigned char dp[8] = {00,00, 00,00, 0x10,00, 00,00}; 
__constant__ unsigned char dc[8] = {00,00, 0x20,00, 00,0x02, 00,0x20};




// 4-bit Sbox
__constant__ unsigned char sbox_4[16] = {12,6,9,0,1,10,2,11,3,8,5,13,4,14,7,15};
__constant__ unsigned char sbox_4_inv[16] = {3,4,6,8,12,10,1,14,9,2,5,7,0,11,13,15};


// ShiftAndSwitchRows permutation
__constant__ unsigned char P[16] = {0,1,2,3,7,4,5,6,10,11,8,9,13,14,15,12};
__constant__ unsigned char P_inv[16] = {0,1,2,3,5,6,7,4,10,11,8,9,15,12,13,14};

// Tweakey permutation
__constant__ unsigned char TWEAKEY_P[16] = {9,15,8,13,10,14,12,11,0,1,2,3,4,5,6,7};
__constant__ unsigned char TWEAKEY_P_inv[16] = {8,9,10,11,12,13,14,15,2,0,4,7,6,3,5,1};

// round constants
__constant__ unsigned char RC[62] = {
		0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3E, 0x3D, 0x3B, 0x37, 0x2F,
		0x1E, 0x3C, 0x39, 0x33, 0x27, 0x0E, 0x1D, 0x3A, 0x35, 0x2B,
		0x16, 0x2C, 0x18, 0x30, 0x21, 0x02, 0x05, 0x0B, 0x17, 0x2E,
		0x1C, 0x38, 0x31, 0x23, 0x06, 0x0D, 0x1B, 0x36, 0x2D, 0x1A,
		0x34, 0x29, 0x12, 0x24, 0x08, 0x11, 0x22, 0x04, 0x09, 0x13,
		0x26, 0x0c, 0x19, 0x32, 0x25, 0x0a, 0x15, 0x2a, 0x14, 0x28,
		0x10, 0x20};

__device__ void AddKey(unsigned char state[4][4], unsigned char keyCells[4][4][4])
{
    int i, j, k;
    unsigned char pos;
    unsigned char keyCells_tmp[4][4][4];


    for(i = 0; i <= 1; i++)
    {
        for(j = 0; j < 4; j++)
        {
            state[i][j] ^= keyCells[0][i][j];
            state[i][j] ^= keyCells[1][i][j] ^ keyCells[2][i][j] ^ keyCells[3][i][j];
        }
    }


    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){     
                pos=TWEAKEY_P[j+4*i];
                keyCells_tmp[k][i][j]=keyCells[k][pos>>2][pos&0x3];
            }
        }
    }

    for(k = 0; k <4; k++){
        for(i = 0; i <= 1; i++){
            for(j = 0; j < 4; j++){
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
                    if(version == 1)
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<1)&0xC)|(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],2))<<1)&0x2)|(TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],2)^TAKE_BIT(keyCells_tmp[k][i][j],3));
                    //v2
                    if(version == 2)
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]<<2)&0xC)|(((TAKE_BIT(keyCells_tmp[k][i][j],3)^TAKE_BIT(keyCells_tmp[k][i][j],2))<<1)&0x2)|(TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],2));
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
}


__device__ void AddKey_inv(unsigned char state[4][4], unsigned char keyCells[3][4][4])
{
    int i, j, k;
    unsigned char pos;
    unsigned char keyCells_tmp[4][4][4];
    for(k = 0; k <4; k++){
        for(i = 0; i < 4; i++){
            for(j = 0; j < 4; j++){                
                pos=TWEAKEY_P_inv[j+4*i];
                keyCells_tmp[k][i][j]=keyCells[k][pos>>2][pos&0x3];
            }
        }
    }

    for(k = 0; k <4; k++){
        for(i = 2; i <= 3; i++){
            for(j = 0; j < 4; j++){
           
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
                    if(version == 1)
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>1)&0x7)^(TAKE_BIT(keyCells_tmp[k][i][j],3)&0x1)^(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],2)^TAKE_BIT(keyCells_tmp[k][i][j],3))<<3)&0x8);
                    //v2 inv
                    if(version == 2)
                        keyCells_tmp[k][i][j]=((keyCells_tmp[k][i][j]>>2)&0x3)^(((TAKE_BIT(keyCells_tmp[k][i][j],3)^TAKE_BIT(keyCells_tmp[k][i][j],1)^TAKE_BIT(keyCells_tmp[k][i][j],0))<<3)&0x8)^(((TAKE_BIT(keyCells_tmp[k][i][j],0)^TAKE_BIT(keyCells_tmp[k][i][j],3))<<2)&0x4);
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

    for(i = 0; i <= 1; i++)
    {
        for(j = 0; j < 4; j++)
        {
            state[i][j] ^= keyCells[0][i][j];
            state[i][j] ^= keyCells[1][i][j] ^ keyCells[2][i][j] ^ keyCells[3][i][j];
        }
    }
}


__device__ void AddConstants(unsigned char state[4][4], int r)
{
	state[0][0] ^= (RC[r] & 0xf);
	state[1][0] ^= ((RC[r]>>4) & 0x3);
	state[2][0] ^= 0x2;
}

__device__ void SubCell4(unsigned char state[4][4])
{
	int i,j;
	for(i = 0; i < 4; i++)
		for(j = 0; j <  4; j++)
			state[i][j] = sbox_4[state[i][j]];
}


__device__ void SubCell4_inv(unsigned char state[4][4])
{
	int i,j;
	for(i = 0; i < 4; i++)
		for(j = 0; j <  4; j++)
			state[i][j] = sbox_4_inv[state[i][j]];
}


__device__ void ShiftRows(unsigned char state[4][4])
{
	int i, j, pos;

	unsigned char state_tmp[4][4];
    for(i = 0; i < 4; i++)
    {
        for(j = 0; j < 4; j++)
        {
            pos=P[j+4*i];
            state_tmp[i][j]=state[pos>>2][pos&0x3];
        }
    }

    for(i = 0; i < 4; i++)
    {
        for(j = 0; j < 4; j++)
        {
            state[i][j]=state_tmp[i][j];
        }
    }
}

__device__ void ShiftRows_inv(unsigned char state[4][4])
{
	int i, j, pos;

	unsigned char state_tmp[4][4];
    for(i = 0; i < 4; i++)
    {
        for(j = 0; j < 4; j++)
        {
            pos=P_inv[j+4*i];
            state_tmp[i][j]=state[pos>>2][pos&0x3];
        }
    }

    for(i = 0; i < 4; i++)
    {
        for(j = 0; j < 4; j++)
        {
            state[i][j]=state_tmp[i][j];
        }
    }
}


__device__ void MixColumn(unsigned char state[4][4])
{
	int j;
    unsigned char temp;

	for(j = 0; j < 4; j++){
        state[1][j]^=state[2][j];
        state[2][j]^=state[0][j];
        state[3][j]^=state[2][j];

        temp=state[3][j];
        state[3][j]=state[2][j];
        state[2][j]=state[1][j];
        state[1][j]=state[0][j];
        state[0][j]=temp;
	}
}


__device__ void MixColumn_inv(unsigned char state[4][4])
{
	int j;
    unsigned char temp;

	for(j = 0; j < 4; j++){
        temp=state[3][j];
        state[3][j]=state[0][j];
        state[0][j]=state[1][j];
        state[1][j]=state[2][j];
        state[2][j]=temp;

        state[3][j]^=state[2][j];
        state[2][j]^=state[0][j];
        state[1][j]^=state[2][j];
	}
}

__device__ void ENC(unsigned char* input, const unsigned char* userkey, int r)
{
	unsigned char state[4][4];
	unsigned char keyCells[4][4][4];
	int i;

	for(i=0; i<4; i++)
		for(int j=0; j<4; j++)
			for(int k=0; k<4; k++)
				keyCells[i][j][k]=0;                                 
	for(i = 0; i < 16; i++) {
            if(i&1)
            {
                state[i>>2][i&0x3] = input[i>>1]&0xF;
                keyCells[0][i>>2][i&0x3] = userkey[i>>1]&0xF;
                keyCells[1][i>>2][i&0x3] = userkey[(i+16)>>1]&0xF;                
                keyCells[2][i>>2][i&0x3] = userkey[(i+32)>>1]&0xF;
                keyCells[3][i>>2][i&0x3] = userkey[(i+48)>>1]&0xF;
            }
            else
            {
                state[i>>2][i&0x3] = (input[i>>1]>>4)&0xF;
                keyCells[0][i>>2][i&0x3] = (userkey[i>>1]>>4)&0xF;
                keyCells[1][i>>2][i&0x3] = (userkey[(i+16)>>1]>>4)&0xF;
                keyCells[2][i>>2][i&0x3] = (userkey[(i+32)>>1]>>4)&0xF;
                keyCells[3][i>>2][i&0x3] = (userkey[(i+48)>>1]>>4)&0xF;
            }
    }


	for(i = 0; i < r; i++){
        SubCell4(state);

 
        AddConstants(state, i);
           
        AddKey(state, keyCells);
         
        ShiftRows(state);
            
        MixColumn(state);
           
		
	}

	
        for(i = 0; i < 8; i++)
            input[i] = ((state[(2*i)>>2][(2*i)&0x3] & 0xF) << 4) | (state[(2*i+1)>>2][(2*i+1)&0x3] & 0xF);


}


__device__ void DEC(unsigned char* input, const unsigned char* userkey, int r)
{
	unsigned char state[4][4];
	unsigned char dummy[4][4]={{0}};
	unsigned char keyCells[4][4][4];
	int i;

    	for(i=0; i<4; i++)
		for(int j=0; j<4; j++)
			for(int k=0; k<4; k++)
				keyCells[i][j][k]=0;
	for(i = 0; i < 16; i++) {
 
            if(i&1)
            {
                state[i>>2][i&0x3] = input[i>>1]&0xF;
                keyCells[0][i>>2][i&0x3] = userkey[i>>1]&0xF;
                keyCells[1][i>>2][i&0x3] = userkey[(i+16)>>1]&0xF;
                keyCells[2][i>>2][i&0x3] = userkey[(i+32)>>1]&0xF;
                keyCells[3][i>>2][i&0x3] = userkey[(i+48)>>1]&0xF;
            }
            else
            {
                state[i>>2][i&0x3] = (input[i>>1]>>4)&0xF;
                keyCells[0][i>>2][i&0x3] = (userkey[i>>1]>>4)&0xF;
                keyCells[1][i>>2][i&0x3] = (userkey[(i+16)>>1]>>4)&0xF;
                keyCells[2][i>>2][i&0x3] = (userkey[(i+32)>>1]>>4)&0xF;
                keyCells[3][i>>2][i&0x3] = (userkey[(i+48)>>1]>>4)&0xF;
            }
        
    }

    for(i = r-1; i >=0 ; i--){
        AddKey(dummy, keyCells);
    }

    

	for(i = r-1; i >=0 ; i--){
        MixColumn_inv(state);
            
        ShiftRows_inv(state);
           
        AddKey_inv(state, keyCells);
           
        AddConstants(state, i);
            
        SubCell4_inv(state);

            
	}

        for(i = 0; i < 8; i++)
            input[i] = ((state[(2*i)>>2][(2*i)&0x3] & 0xF) << 4) | (state[(2*i+1)>>2][(2*i+1)&0x3] & 0xF);
 

}

//-------------------generate random numbers-------//
__device__ float generate(curandState *globalState, int ind)
{
	curandState localState = globalState[ind];
	float RANDOM = curand_uniform(&localState);// uniform distribution
	globalState[ind] = localState;
	return RANDOM;
}

__global__ void setup_kernel(curandState *state, unsigned long seed)
{
	int ix = threadIdx.x + blockIdx.x*blockDim.x;
	int iy = threadIdx.y + blockIdx.y*blockDim.y;
	int idx = iy * blockDim.x*gridDim.x + ix;
	
	curand_init(seed, idx, 0, &state[idx]);// initialize the state
}

//-------------This is our kernel function where the random numbers generated------//
__global__ void our_kernel(curandState *globalState,int *devNum)
{
	int ix = threadIdx.x + blockIdx.x*blockDim.x;
	int iy = threadIdx.y + blockIdx.y*blockDim.y;
	int idx = iy * blockDim.x*gridDim.x + ix;

	int i,j,jj;
	int num=0;
	unsigned char p1[8],p2[8];
	unsigned char c3[8],c4[8];
	unsigned char k1[32], k2[32], k3[32], k4[32];
	bool flag;
	
	int k;
	// randomly choose k1
	for(i = 0; i < 32; i++) 
	{
		k = generate(globalState, idx) * 100000;
		k1[i] = k & 0xff;
		//printf("k1[%d]=%d\n", i,k1[i]);
	}	
	for(i = 0; i < 32; i++) 
		k2[i] = k1[i]^dk1[i];	
	for(i = 0; i < 32; i++) 
		k3[i] = k1[i]^dk2[i];	
	for(i = 0; i < 32; i++) 
		k4[i] = k2[i]^dk2[i];
	
	for (j=0; j<N1; j++)
	{
	for (jj=0; jj<N2; jj++)
	{
		//printf("[%d]=%d\n",idx,j);
		// randomly choose p1
		for(i = 0; i < 8; i++) 
		{
			k = generate(globalState, idx) * 100000;
			p1[i] = k & 0xff;
			//printf("P1[%d]=%d\n", i,p1[i]);	
		}
		// derive p2
		for(i = 0; i < 8; i++) 
			p2[i] = p1[i]^dp[i];	

		ENC(p1, k1, r);
		ENC(p2, k2, r);
		
		// derive c3
		for(i = 0; i < 8; i++) 
			c3[i] = p1[i]^dc[i];
		// derive c4
		for(i = 0; i < 8; i++) 
			c4[i] = p2[i]^dc[i];
		DEC(c3, k3, r);
		DEC(c4, k4, r);
		flag = 1;
		for(i = 0; i < 8; i++)
		{
			//printf("c3=%d, c4=%d\n", c3[i],c4[i]);	
			if ((c3[i]^c4[i]) != dp[i])
				flag = 0;
		}
		if (flag) 
		{
			num ++; 
		}		
		//printf("%d\n", k);
	}
	}
	
	devNum[idx]=num;
	//printf("%d\n",devNum[idx]);
}

int main()
{
	int blockx = 256;
	int blocky = 1;
	dim3 block(blockx, blocky);

	int gridx = 1;
	int gridy = 128;
	dim3 grid(gridx,gridy); 

	int N = gridx*gridy*blockx*blocky;// the number of states
	int *hostNum = (int *)malloc(N * sizeof(int));
	double sum=0;

	cudaEvent_t start, stop;
	float elapsedTime = 0.0;
	cudaEventCreate(&start);
	cudaEventCreate(&stop);

	cudaEventRecord(start, 0);

	//--------------------//
	curandState* devStates;
	int* devNum;
	cudaError_t err = cudaSuccess;
	err=cudaMalloc(&devStates, N * sizeof(curandState));
	err=cudaMalloc((void **)&devNum, N * sizeof(int));
	if(err!=cudaSuccess)
    	{
        	printf("the cudaMalloc on GPU is failed\n");
        	return 1;
    	}
	printf("SUCCESS\n");

	srand(time(0));
	int seed = rand();

	//  Initialize the states
	setup_kernel <<<grid, block>>> (devStates, seed);

	our_kernel <<<grid, block >>> (devStates,devNum);
 	
	err = cudaGetLastError();

    	if (err != cudaSuccess)
    	{
        	fprintf(stderr, "Failed to launch vectorAdd kernel (error code %s)!\n", cudaGetErrorString(err));
        	exit(EXIT_FAILURE);
   	}
	
	err=cudaMemcpy(hostNum,devNum,N*sizeof(int),cudaMemcpyDeviceToHost);
	
	if (err != cudaSuccess)
   	{
        	fprintf(stderr, "Failed to copy vector C from device to host (error code %s)!\n", cudaGetErrorString(err));
        	exit(EXIT_FAILURE);
    	}

	for(int i=0;i<N;i++)
		sum += hostNum[i];
	sum = double(N)/sum;
	//cout <<  log(sum)/log(2.0) << endl;
	cout <<  (-log(sum)/log(2.0)-log(hN1)/log(2.0)-log(hN2)/log(2.0)) << endl;

	cudaFree(devNum);
	cudaFree(devStates);

	cudaEventRecord(stop, 0);
	cudaEventSynchronize(stop);
	cudaEventElapsedTime(&elapsedTime, start, stop);

	cout << (elapsedTime/1000) <<'s'<< endl; 

	cudaEventDestroy(start);
	cudaEventDestroy(stop);
	cudaDeviceReset();
	return 0;
}
