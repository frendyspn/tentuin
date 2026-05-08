'use server'

import { revalidatePath } from 'next/cache'
import { createAdminServerClient } from '@/lib/supabase/server'
import { createSchool, updateSchool, setSchoolTarget } from '@tentuin/supabase'
import type { SchoolFormValues } from '@/lib/validators/school.schema'
import type { TargetFormValues } from '@/lib/validators/target.schema'

async function getToken() {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()
  if (!session) throw new Error('Tidak terautentikasi')
  return session.access_token
}

export async function createSchoolAction(data: SchoolFormValues) {
  const token = await getToken()
  await createSchool(
    {
      name: data.name,
      npsn: null,
      city: data.city,
      province: data.province,
      total_students: data.total_students,
      address: data.address ?? null,
      email: data.email || null,
      phone: data.phone ?? null,
      logo_url: null,
      is_active: data.is_active,
    },
    token,
  )
  revalidatePath('/sekolah')
}

export async function updateSchoolAction(schoolId: string, data: SchoolFormValues) {
  const token = await getToken()
  await updateSchool(
    schoolId,
    {
      name: data.name,
      city: data.city,
      province: data.province,
      total_students: data.total_students,
      address: data.address ?? null,
      email: data.email || null,
      phone: data.phone ?? null,
      is_active: data.is_active,
    },
    token,
  )
  revalidatePath('/sekolah')
  revalidatePath(`/sekolah/${schoolId}`)
}

export async function setSchoolTargetAction(
  schoolId: string,
  year: number,
  data: TargetFormValues,
) {
  const token = await getToken()
  await setSchoolTarget(schoolId, year, data.annual_target, data.monthly_targets ?? null, token)
  revalidatePath(`/sekolah/${schoolId}`)
}
