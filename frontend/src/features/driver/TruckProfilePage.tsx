import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Truck, Save } from 'lucide-react'
import { usersApi } from '@/api/users'
import { Button } from '@/components/ui/Button'

interface Field {
  key: 'grossWeight' | 'height' | 'width' | 'length'
  label: string
  unit: string
  min: number
  max: number
  placeholder: string
}

const FIELDS: Field[] = [
  { key: 'grossWeight', label: 'Gross weight', unit: 'kg', min: 1000, max: 100000, placeholder: '12000' },
  { key: 'height',      label: 'Height',       unit: 'cm', min: 100,  max: 600,    placeholder: '400' },
  { key: 'width',       label: 'Width',        unit: 'cm', min: 100,  max: 350,    placeholder: '250' },
  { key: 'length',      label: 'Length',       unit: 'cm', min: 200,  max: 2500,   placeholder: '1200' },
]

export function TruckProfilePage() {
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['truck-profile'],
    queryFn: () => usersApi.getTruckProfile().then(r => r.data.payload),
  })

  const [form, setForm] = useState({
    grossWeight: '',
    height: '',
    width: '',
    length: '',
  })
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (data) {
      setForm({
        grossWeight: data.grossWeight?.toString() ?? '',
        height:      data.height?.toString() ?? '',
        width:       data.width?.toString() ?? '',
        length:      data.length?.toString() ?? '',
      })
    }
  }, [data])

  const mutation = useMutation({
    mutationFn: () => usersApi.updateTruckProfile({
      grossWeight: form.grossWeight ? Number(form.grossWeight) : null,
      height:      form.height      ? Number(form.height)      : null,
      width:       form.width       ? Number(form.width)       : null,
      length:      form.length      ? Number(form.length)      : null,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['truck-profile'] })
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
    },
  })

  const handleChange = (key: string, value: string) => {
    setSaved(false)
    setForm(prev => ({ ...prev, [key]: value }))
  }

  if (isLoading) return (
    <div className="flex items-center justify-center h-full">
      <div className="w-6 h-6 border-2 border-brand border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="max-w-md mx-auto p-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-xl bg-brand/10 flex items-center justify-center">
          <Truck size={20} className="text-brand" />
        </div>
        <div>
          <h1 className="text-lg font-semibold text-text-primary">Truck Profile</h1>
          <p className="text-xs text-text-muted">Used for route calculation</p>
        </div>
      </div>

      <div className="bg-bg-surface border border-bg-border rounded-xl p-4 space-y-4">
        {FIELDS.map(f => (
          <div key={f.key}>
            <label className="block text-xs font-medium text-text-secondary mb-1.5">
              {f.label}
              <span className="ml-1 text-text-muted font-normal">({f.unit})</span>
            </label>
            <div className="relative">
              <input
                type="number"
                min={f.min}
                max={f.max}
                value={form[f.key]}
                onChange={e => handleChange(f.key, e.target.value)}
                placeholder={f.placeholder}
                className="w-full bg-bg-base border border-bg-border rounded-lg px-3 py-2 text-sm text-text-primary placeholder:text-text-muted outline-none focus:border-brand transition-colors pr-12"
              />
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-muted pointer-events-none">
                {f.unit}
              </span>
            </div>
            <p className="text-[10px] text-text-muted mt-1">
              {f.min}–{f.max} {f.unit}
            </p>
          </div>
        ))}

        <p className="text-xs text-text-muted pt-1 border-t border-bg-border">
          Leave fields empty to use default values when starting navigation.
        </p>

        <Button
          onClick={() => mutation.mutate()}
          loading={mutation.isPending}
          className="w-full"
        >
          {saved ? '✓ Saved' : (
            <>
              <Save size={15} />
              Save profile
            </>
          )}
        </Button>

        {mutation.isError && (
          <p className="text-xs text-danger text-center">
            {(mutation.error as any)?.response?.data?.message ?? 'Save failed'}
          </p>
        )}
      </div>
    </div>
  )
}
