#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <fstream>
#include <omp.h>
#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

ofstream outfile("measures.txt", ios_base::app);

void OnMult(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	Time1 = clock();

	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = 0;
			for (k = 0; k < m_ar; k++)
			{
				temp += pha[i * m_ar + k] * phb[k * m_br + j];
			}
			phc[i * m_ar + j] = temp;
		}
	}

	Time2 = clock();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}

void OnMultLine(int m_ar, int m_br)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = 0.0;

	Time1 = clock();

	for (i = 0; i < m_ar; i++)
	{
		for (k = 0; k < m_br; k++)
		{
			for (j = 0; j < m_ar; j++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
			}
		}
	}

	Time2 = clock();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}

void OnMultBlock(int m_ar, int m_br, int bk_size)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k, bi, bj, bk;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = 0.0;

	Time1 = clock();

	for (bi = 0; bi < m_ar; bi += bk_size)
	{
		for (bj = 0; bj < m_br; bj += bk_size)
		{
			for (bk = 0; bk < m_ar; bk += bk_size)
			{
				for (i = 0; i < bk_size; i++)
				{
					for (j = 0; j < bk_size; j++)
					{
						for (k = 0; k < bk_size; k++)
						{
							phc[(bi + i) * m_ar + (bj + j)] += pha[(bi + i) * m_ar + (bk + k)] * phb[(bk + k) * m_br + (bj + j)];
						}
					}
				}
			}
		}
	}

	Time2 = clock();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}

void OnMultLineParallel(int m_ar, int m_br)
{
	double Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = 0.0;

	Time1 = omp_get_wtime();

	#pragma omp parallel for
	for (i = 0; i < m_ar; i++)
	{
		for (k = 0; k < m_br; k++)
		{
			for (j = 0; j < m_ar; j++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
			}
		}
	}

	Time2 = omp_get_wtime();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1));
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (int i = 0; i < 1; i++)
	{
		for (int j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}

void OnMultLineParallel2(int m_ar, int m_br)
{
	double Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = 0.0;

	Time1 = omp_get_wtime();

	#pragma omp parallel
	for (i = 0; i < m_ar; i++)
	{
		for (k = 0; k < m_br; k++)
		{
			#pragma omp for
			for (j = 0; j < m_ar; j++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
			}
		}
	}

	Time2 = omp_get_wtime();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1));
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (int i = 0; i < 1; i++)
	{
		for (int j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}

void OnMultLineParallel3(int m_ar, int m_br)
{
	double Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phc[i * m_br + j] = 0.0;

	Time1 = omp_get_wtime();

// TBD
	for (i = 0; i < m_ar; i++)
	{
		for (k = 0; k < m_br; k++)
		{
			for (j = 0; j < m_ar; j++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
			}
		}
	}

	Time2 = omp_get_wtime();
	cout << "Dimensions: " << m_ar << 'x' << m_br << endl;
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1));
	cout << st;

	outfile << "Dimensions: " << m_ar << 'x' << m_br << endl;
	outfile << st << endl;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	outfile << "Result matrix: " << endl;
	for (int i = 0; i < 1; i++)
	{
		for (int j = 0; j < min(10, m_br); j++)
			{
				cout << phc[j] << " ";
				outfile << phc[j] << " ";
			}
	}
	cout << endl;
	outfile << endl;

	free(pha);
	free(phb);
	free(phc);
}


void handle_error(int retval)
{
	printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
	exit(1);
}

void init_papi()
{
	int retval = PAPI_library_init(PAPI_VER_CURRENT);
	if (retval != PAPI_VER_CURRENT && retval < 0)
	{
		printf("PAPI library version mismatch!\n");
		exit(1);
	}
	if (retval < 0)
		handle_error(retval);

	std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
			  << " MINOR: " << PAPI_VERSION_MINOR(retval)
			  << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main(int argc, char *argv[])
{

	char c;
	int blockSize;
	int start, end, step;
	int op;
	int repetions;
	
	
	int EventSet = PAPI_NULL;
	
	long long values[2];
	int ret;

	ret = PAPI_library_init(PAPI_VER_CURRENT);
	if (ret != PAPI_VER_CURRENT)
		std::cout << "FAIL" << endl;

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK)
		cout << "ERROR: create eventset" << endl;

	ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
	if (ret != PAPI_OK)
		cout << "ERROR: PAPI_L1_DCM" << endl;

	ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		cout << "ERROR: PAPI_L2_DCM" << endl;
	

	op = 1;
	do
	{
		cout << endl
			 << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Line Multiplication Parallel" << endl;
		cout << "5. Line Multiplication Parallel 2" << endl;
		cout << "6. Line Multiplication Parallel 3" << endl;
		cout << "0. Exit" << endl;
		cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;

		switch (op)
		{
		case 1:
			outfile << "Multiplication" << endl;
			printf("Starting Dimensions 'line=cols' (600): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (3000): ");
			cin >> end;
			printf("Step (400): ");
			cin >> step;
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
				for (int i = start; i <= end; i += step)
				{
					cout << endl;
					outfile << endl;

					
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK)
						cout << "ERROR: Start PAPI" << endl;
						
					OnMult(i, i);

					
					ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK)
						cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n", values[0]);
					printf("L2 DCM: %lld \n", values[1]);
					outfile << "L1 DCM: " << values[0] << endl;
					outfile << "L2 DCM: " << values[1] << endl;


					ret = PAPI_reset(EventSet);
					if (ret != PAPI_OK)
						std::cout << "FAIL reset" << endl;
					
				}
			}
			cout << endl;
			break;
		case 2:
			outfile << "Line Multiplication" << endl;
			printf("Starting Dimensions 'line=cols' (4096): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (10240): ");
			cin >> end;
			printf("Step (2048): ");
			cin >> step;
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
					for (int i = start; i <= end; i += step)
					{
						cout << endl;
						outfile << endl;

						
						ret = PAPI_start(EventSet);
						if (ret != PAPI_OK)
							cout << "ERROR: Start PAPI" << endl;
						

						OnMultLine(i, i);
						
						
						ret = PAPI_stop(EventSet, values);
						if (ret != PAPI_OK)
							cout << "ERROR: Stop PAPI" << endl;
						printf("L1 DCM: %lld \n", values[0]);
						printf("L2 DCM: %lld \n", values[1]);
						outfile << "L1 DCM: " << values[0] << endl;
						outfile << "L2 DCM: " << values[1] << endl;

						ret = PAPI_reset(EventSet);
						if (ret != PAPI_OK)
							std::cout << "FAIL reset" << endl;
						
					}
			}
			cout << endl;
			break;
		case 3:
		{
			outfile << "Block Multiplication" << endl;
			printf("Starting Dimensions 'line=cols' (4096): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (10240): ");
			cin >> end;
			printf("Step (2048): ");
			cin >> step;
			cout << "Block size (64 128 256 512 1024): " << endl;
			cin.ignore();
			string input;
			getline(cin, input);
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
				istringstream is(input);
				
				while (is >> blockSize)
				{
					for (int i = start; i <= end; i += step)
					{
						cout << endl;
						outfile << endl;
						cout << "Block Size: " << blockSize;
						outfile << "Block Size: " << blockSize;

						
						ret = PAPI_start(EventSet);
						if (ret != PAPI_OK)
							cout << "ERROR: Start PAPI" << endl;
						

						OnMultBlock(i, i, blockSize);
						
						
						ret = PAPI_stop(EventSet, values);
						if (ret != PAPI_OK)
							cout << "ERROR: Stop PAPI" << endl;
						printf("L1 DCM: %lld \n", values[0]);
						printf("L2 DCM: %lld \n", values[1]);
						outfile << "L1 DCM: " << values[0] << endl;
						outfile << "L2 DCM: " << values[1] << endl;

						ret = PAPI_reset(EventSet);
						if (ret != PAPI_OK)
							std::cout << "FAIL reset" << endl;
						
					}
				}
			}
			cout << endl;
			break;
		}
		case 4:
			outfile << "Line Multiplication Parallel" << endl;
			printf("Starting Dimensions 'line=cols' (4096): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (10240): ");
			cin >> end;
			printf("Step (2048): ");
			cin >> step;
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
				for (int i = start; i <= end; i += step)
				{
					cout << endl;
					outfile << endl;

					
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK)
						cout << "ERROR: Start PAPI" << endl;
					

					OnMultLineParallel(i, i);
					
					
					ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK)
						cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n", values[0]);
					printf("L2 DCM: %lld \n", values[1]);
					outfile << "L1 DCM: " << values[0] << endl;
					outfile << "L2 DCM: " << values[1] << endl;

					ret = PAPI_reset(EventSet);
					if (ret != PAPI_OK)
						std::cout << "FAIL reset" << endl;
					
				}
			}
			cout << endl;
			break;
		case 5:
			outfile << "Line Multiplication Parallel 2" << endl;
			printf("Starting Dimensions 'line=cols' (4096): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (10240): ");
			cin >> end;
			printf("Step (2048): ");
			cin >> step;
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
				for (int i = start; i <= end; i += step)
				{
					cout << endl;
					outfile << endl;

					
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK)
						cout << "ERROR: Start PAPI" << endl;
					

					OnMultLineParallel2(i, i);
					
					
					ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK)
						cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n", values[0]);
					printf("L2 DCM: %lld \n", values[1]);
					outfile << "L1 DCM: " << values[0] << endl;
					outfile << "L2 DCM: " << values[1] << endl;

					ret = PAPI_reset(EventSet);
					if (ret != PAPI_OK)
						std::cout << "FAIL reset" << endl;
					
				}
			}
			cout << endl;
			break;

		case 6:
			outfile << "Line Multiplication Parallel 3" << endl;
			printf("Starting Dimensions 'line=cols' (4096): ");
			cin >> start;
			printf("Ending Dimensions 'line=cols' (10240): ");
			cin >> end;
			printf("Step (2048): ");
			cin >> step;
			printf("Repetions (3): ");
			cin >> repetions;
			for(int j = 0; j < repetions; j++){
				printf("Repetition %d", j);
				outfile << "Repetition " << j << endl;
				for (int i = start; i <= end; i += step)
				{
					cout << endl;
					outfile << endl;

					
					ret = PAPI_start(EventSet);
					if (ret != PAPI_OK)
						cout << "ERROR: Start PAPI" << endl;
					

					OnMultLineParallel3(i, i);
					
					
					ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK)
						cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n", values[0]);
					printf("L2 DCM: %lld \n", values[1]);
					outfile << "L1 DCM: " << values[0] << endl;
					outfile << "L2 DCM: " << values[1] << endl;

					ret = PAPI_reset(EventSet);
					if (ret != PAPI_OK)
						std::cout << "FAIL reset" << endl;
					
				}
			}
			cout << endl;
			break;
		}

	} while (op != 0);

	
	ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_destroy_eventset(&EventSet);
	if (ret != PAPI_OK)
		std::cout << "FAIL destroy" << endl;
	
}
