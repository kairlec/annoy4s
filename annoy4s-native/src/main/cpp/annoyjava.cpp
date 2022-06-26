#include "annoylib.h"
#include "kissrandom.h"

using namespace Annoy;

namespace Annoy {
	template<typename Random, typename ThreadedBuildPolicy>
	class HammingWrapper : public AnnoyIndexInterface<int32_t, float> {
		// Wrapper class for Hamming distance, using composition.
		// This translates binary (float) vectors into packed uint64_t vectors.
		// This is questionable from a performance point of view. Should reconsider this solution.
	private:
		int32_t _f_external, _f_internal;
		AnnoyIndex<int32_t, uint64_t, Hamming, Random, ThreadedBuildPolicy> _index;
		void _pack(const float* src, uint64_t* dst) const {
			for (int32_t i = 0; i < _f_internal; i++) {
				dst[i] = 0;
				for (int32_t j = 0; j < 64 && i * 64 + j < _f_external; j++) {
					dst[i] |= (uint64_t)(src[i * 64 + j] > 0.5) << j;
				}
			}
		};
		void _unpack(const uint64_t* src, float* dst) const {
			for (int32_t i = 0; i < _f_external; i++) {
				dst[i] = (src[i / 64] >> (i % 64)) & 1;
			}
		};
	public:
		HammingWrapper(int f) : _f_external(f), _f_internal((f + 63) / 64), _index((f + 63) / 64) {};
		bool add_item(int32_t item, const float* w, char** error) {
			vector<uint64_t> w_internal(_f_internal, 0);
			_pack(w, &w_internal[0]);
			return _index.add_item(item, &w_internal[0], error);
		};
		bool build(int q, int n_threads, char** error) { return _index.build(q, n_threads, error); };
		bool unbuild(char** error) { return _index.unbuild(error); };
		bool save(const char* filename, bool prefault, char** error) { return _index.save(filename, prefault, error); };
		void unload() { _index.unload(); };
		bool load(const char* filename, bool prefault, char** error) { return _index.load(filename, prefault, error); };
		float get_distance(int32_t i, int32_t j) const { return _index.get_distance(i, j); };
		void get_nns_by_item(int32_t item, size_t n, int search_k, vector<int32_t>* result, vector<float>* distances) const {
			if (distances) {
				vector<uint64_t> distances_internal;
				_index.get_nns_by_item(item, n, search_k, result, &distances_internal);
				distances->insert(distances->begin(), distances_internal.begin(), distances_internal.end());
			}
			else {
				_index.get_nns_by_item(item, n, search_k, result, NULL);
			}
		};
		void get_nns_by_vector(const float* w, size_t n, int search_k, vector<int32_t>* result, vector<float>* distances) const {
			vector<uint64_t> w_internal(_f_internal, 0);
			_pack(w, &w_internal[0]);
			if (distances) {
				vector<uint64_t> distances_internal;
				_index.get_nns_by_vector(&w_internal[0], n, search_k, result, &distances_internal);
				distances->insert(distances->begin(), distances_internal.begin(), distances_internal.end());
			}
			else {
				_index.get_nns_by_vector(&w_internal[0], n, search_k, result, NULL);
			}
		};
		int32_t get_n_items() const { return _index.get_n_items(); };
		int32_t get_n_trees() const { return _index.get_n_trees(); };
		void verbose(bool v) { _index.verbose(v); };
		void get_item(int32_t item, float* v) const {
			vector<uint64_t> v_internal(_f_internal, 0);
			_index.get_item(item, &v_internal[0]);
			_unpack(&v_internal[0], v);
		};
		void set_seed(uint64_t q) { _index.set_seed(q); };
		bool on_disk_build(const char* filename, char** error) { return _index.on_disk_build(filename, error); };
	};
}


#ifdef _MSC_VER
#define DllExport   __declspec( dllexport )
#else
#define DllExport
#endif // _MSC_VER
extern "C" {

	DllExport AnnoyIndexInterface<int32_t, float>* createAngular(int f) {
		return new AnnoyIndex<int32_t, float, Angular, Kiss64Random, AnnoyIndexSingleThreadedBuildPolicy>(f);
	}

	DllExport AnnoyIndexInterface<int32_t, float>* createEuclidean(int f) {
		return new AnnoyIndex<int32_t, float, Euclidean, Kiss64Random, AnnoyIndexSingleThreadedBuildPolicy>(f);
	}

	DllExport AnnoyIndexInterface<int32_t, float>* createManhattan(int f) {
		return new AnnoyIndex<int32_t, float, Manhattan, Kiss64Random, AnnoyIndexSingleThreadedBuildPolicy>(f);
	}

	DllExport AnnoyIndexInterface<int32_t, float>* createHamming (int f) {
		return new HammingWrapper<Kiss64Random, AnnoyIndexSingleThreadedBuildPolicy>(f);
	}

	DllExport void deleteIndex(AnnoyIndexInterface<int32_t, float>* ptr) {
		delete ptr;
	}

	DllExport void addItem(AnnoyIndexInterface<int32_t, float>* ptr, int item, float* w) {
		ptr->add_item(item, w);
	}

	DllExport void build(AnnoyIndexInterface<int32_t, float>* ptr, int q) {
		ptr->build(q);
	}

	DllExport bool save(AnnoyIndexInterface<int32_t, float>* ptr, char* filename) {
		return ptr->save(filename);
	}

	DllExport void unload(AnnoyIndexInterface<int32_t, float>* ptr) {
		ptr->unload();
	}

	DllExport bool load(AnnoyIndexInterface<int32_t, float>* ptr, char* filename) {
		return ptr->load(filename);
	}

	DllExport float getDistance(AnnoyIndexInterface<int32_t, float>* ptr, int i, int j) {
		return ptr->get_distance(i, j);
	}

	DllExport void getNnsByItem(AnnoyIndexInterface<int32_t, float>* ptr, int item, int n,
		int search_k, int* result, float* distances) {
		vector<int32_t> resultV;
		vector<float> distancesV;
		ptr->get_nns_by_item(item, n, search_k, &resultV, &distancesV);
		std::copy(resultV.begin(), resultV.end(), result);
		std::copy(distancesV.begin(), distancesV.end(), distances);
	}

	DllExport void getNnsByVector(AnnoyIndexInterface<int32_t, float>* ptr, float* w, int n,
		int search_k, int* result, float* distances) {
		vector<int32_t> resultV;
		vector<float> distancesV;
		ptr->get_nns_by_vector(w, n, search_k, &resultV, &distancesV);
		std::copy(resultV.begin(), resultV.end(), result);
		std::copy(distancesV.begin(), distancesV.end(), distances);
	}

	DllExport int getNItems(AnnoyIndexInterface<int32_t, float>* ptr) {
		return (int)ptr->get_n_items();
	}

	DllExport void verbose(AnnoyIndexInterface<int32_t, float>* ptr, bool v) {
		ptr->verbose(v);
	}

	DllExport void getItem(AnnoyIndexInterface<int32_t, float>* ptr, int item, float* v) {
		ptr->get_item(item, v);
	}
}
